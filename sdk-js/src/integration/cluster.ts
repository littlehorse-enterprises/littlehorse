import { GenericContainer, Network, StartedNetwork, StartedTestContainer, Wait } from 'testcontainers'
import { LHConfig } from '../LHConfig'
import { allocatePort } from './container'

/**
 * A multi-node LittleHorse cluster for tests that a single `lh-standalone`
 * cannot serve.
 *
 * `lh-standalone` bundles its own Kafka, so two of them are two independent
 * clusters, not one cluster with two members. Rebalancing, TLS listeners, and
 * OAuth-authenticated listeners all need real `lh-server` nodes sharing a
 * Kafka — which is what this builds: one Kafka plus N servers on a private
 * Docker network, each published on its own host port.
 *
 * It is deliberately separate from container.ts: these are slow (Kafka plus
 * N JVMs) and only a few tests need them.
 */

const KAFKA_IMAGE = process.env.LH_IT_KAFKA_IMAGE ?? 'apache/kafka:4.1.0'
const LH_SERVER_IMAGE =
  process.env.LH_IT_SERVER_IMAGE ?? 'ghcr.io/littlehorse-enterprises/littlehorse/lh-server:master'

export interface ClusterNode {
  container: StartedTestContainer
  /** Host port this node is reachable on. */
  port: number
  instanceId: number
}

export interface ClusterOptions {
  /** Number of lh-server nodes. */
  nodes: number
  /**
   * Extra environment per node, e.g. TLS or OAuth listener configuration.
   * Receives the node's host port so listener config can reference it.
   */
  extraEnv?: (hostPort: number, instanceId: number) => Record<string, string>
  /** Files to copy into each server container before it starts. */
  files?: Array<{ source: string; target: string }>
  /** Listener name; also the prefix for LHS_LISTENER_<name>_* settings. */
  listenerName?: string
}

export class LHCluster {
  private network?: StartedNetwork
  private kafka?: StartedTestContainer
  readonly nodes: ClusterNode[] = []

  constructor(private readonly options: ClusterOptions) {}

  /** Bootstrap address of the first node. */
  get bootstrapPort(): number {
    if (this.nodes.length === 0) throw new Error('cluster not started')
    return this.nodes[0].port
  }

  config(): LHConfig {
    return LHConfig.fromMap({ LHC_API_HOST: 'localhost', LHC_API_PORT: String(this.bootstrapPort) })
  }

  /** The Docker network, so tests can attach extra containers to it. */
  get dockerNetwork(): StartedNetwork {
    if (this.network === undefined) throw new Error('cluster not started')
    return this.network
  }

  /**
   * Starts the network and Kafka but no servers, so a test can attach other
   * containers (an OAuth issuer, for instance) that the servers must reach by
   * network alias before they boot.
   */
  async startInfra(): Promise<void> {
    this.network = await new Network().start()

    // Single-broker KRaft Kafka. INTERNAL is what the LH servers use; the
    // host never talks to Kafka directly.
    this.kafka = await new GenericContainer(KAFKA_IMAGE)
      .withNetwork(this.network)
      .withNetworkAliases('kafka')
      .withEnvironment({
        KAFKA_LISTENERS: 'CONTROLLER://:29092,INTERNAL://:9092',
        KAFKA_ADVERTISED_LISTENERS: 'INTERNAL://kafka:9092',
        KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'CONTROLLER:PLAINTEXT,INTERNAL:PLAINTEXT',
        KAFKA_INTER_BROKER_LISTENER_NAME: 'INTERNAL',
        KAFKA_CONTROLLER_QUORUM_VOTERS: '1@localhost:29092',
        KAFKA_CONTROLLER_LISTENER_NAMES: 'CONTROLLER',
        KAFKA_PROCESS_ROLES: 'broker,controller',
        KAFKA_NODE_ID: '1',
        KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: '1',
        KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: '1',
        KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: '1',
        KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: '0',
      })
      .withWaitStrategy(Wait.forLogMessage(/Kafka Server started|started \(kafka.server/))
      .withStartupTimeout(180_000)
      .start()
  }

  async start(): Promise<void> {
    await this.startInfra()
    for (let instanceId = 1; instanceId <= this.options.nodes; instanceId++) {
      await this.addNode(instanceId)
    }
  }

  /** Starts one more server into the running cluster. */
  async addNode(instanceId: number): Promise<ClusterNode> {
    if (this.network === undefined) throw new Error('cluster not started')
    const listener = this.options.listenerName ?? 'PLAIN'
    const port = await allocatePort()
    const alias = `lh-server-${instanceId}`

    let container = new GenericContainer(LH_SERVER_IMAGE)
      .withNetwork(this.network)
      .withNetworkAliases(alias)
      .withExposedPorts({ container: port, host: port })
      .withEnvironment({
        LHS_KAFKA_BOOTSTRAP_SERVERS: 'kafka:9092',
        LHS_CLUSTER_ID: 'sdkjs-it-cluster',
        LHS_INSTANCE_ID: String(instanceId),
        LHS_SHOULD_CREATE_TOPICS: 'true',
        LHS_HEALTH_SERVICE_PORT: '1822',
        LHS_INTERNAL_BIND_PORT: '2011',
        LHS_INTERNAL_ADVERTISED_HOST: alias,
        LHS_INTERNAL_ADVERTISED_PORT: '2011',
        LHS_LISTENERS: `${listener}:${port}`,
        // Advertised as localhost:<hostPort> because workers connect from the
        // host, not from inside the Docker network.
        LHS_ADVERTISED_LISTENERS: `${listener}://localhost:${port}`,
        ...(this.options.extraEnv?.(port, instanceId) ?? {}),
      })
      .withStartupTimeout(180_000)

    for (const file of this.options.files ?? []) {
      container = container.withCopyFilesToContainer([{ source: file.source, target: file.target }])
    }

    const node = { container: await container.start(), port, instanceId }
    this.nodes.push(node)
    return node
  }

  /** Stops one node, as a rolling restart or a crash would. */
  async stopNode(instanceId: number): Promise<void> {
    const node = this.nodes.find(n => n.instanceId === instanceId)
    if (node === undefined) throw new Error(`no node ${instanceId}`)
    await node.container.stop({ remove: true, removeVolumes: true })
    this.nodes.splice(this.nodes.indexOf(node), 1)
  }

  async stop(): Promise<void> {
    for (const node of [...this.nodes]) {
      await node.container.stop({ remove: true, removeVolumes: true }).catch(() => {})
    }
    this.nodes.length = 0
    await this.kafka?.stop({ remove: true, removeVolumes: true }).catch(() => {})
    await this.network?.stop().catch(() => {})
  }

  /**
   * Waits until every node answers, using a caller-supplied client factory —
   * needed when the listener requires credentials the default client lacks.
   *
   * Generous by default: a real `lh-server` on a fresh Kafka spends ~2min in
   * Kafka Streams restore before it serves, and much longer when the rest of
   * the integration suite is competing for the same Docker daemon. Keep this
   * strictly below the calling hook's timeout — otherwise the hook expires
   * first and reports only "exceeded timeout", losing the actual error.
   */
  async awaitReadyWith(
    makeClient: (port: number) => Promise<{ putTaskDef: (r: { name: string; inputVars: [] }) => Promise<unknown> }>,
    timeoutMs = 600_000
  ): Promise<void> {
    const deadline = Date.now() + timeoutMs
    for (const node of this.nodes) {
      for (;;) {
        try {
          const client = await makeClient(node.port)
          await client.putTaskDef({ name: `cluster-probe-${node.instanceId}`, inputVars: [] })
          break
        } catch (err) {
          if (Date.now() > deadline) {
            throw new Error(`cluster node ${node.instanceId} never became ready: ${(err as Error).message}`)
          }
          await new Promise(resolve => setTimeout(resolve, 1000))
        }
      }
    }
  }

  /** Waits until every node answers API calls. */
  async awaitReady(timeoutMs = 600_000): Promise<void> {
    const deadline = Date.now() + timeoutMs
    for (const node of this.nodes) {
      const client = LHConfig.fromMap({ LHC_API_HOST: 'localhost', LHC_API_PORT: String(node.port) }).getClient()
      for (;;) {
        try {
          await client.putTaskDef({ name: `cluster-probe-${node.instanceId}`, inputVars: [] })
          break
        } catch (err) {
          if (Date.now() > deadline) {
            throw new Error(`cluster node ${node.instanceId} never became ready: ${(err as Error).message}`)
          }
          await new Promise(resolve => setTimeout(resolve, 1000))
        }
      }
    }
  }
}
