import * as fs from 'fs'
import * as path from 'path'
import { PutWfSpecRequest } from '../proto/service'

const GOLDEN_DIR = path.resolve(__dirname, '../../golden')

/**
 * Loads a golden PutWfSpecRequest produced by the Java SDK (see
 * sdk-js/golden/generator). Golden files are regenerated with:
 *   ./gradlew :sdk-js-golden-generator:run --args="$(pwd)/sdk-js/golden"
 */
export function loadGolden(name: string): PutWfSpecRequest {
  const file = path.join(GOLDEN_DIR, `${name}.json`)
  const json = fs.readFileSync(file, 'utf-8')
  // ignoreUnknownFields: false — if the Java SDK emits a field our generated
  // protos don't know, the goldens are newer than our codegen and the proto
  // definitions must be regenerated (local-dev/compile-proto.sh).
  return PutWfSpecRequest.fromJsonString(json, { ignoreUnknownFields: false })
}

export function listGoldens(): string[] {
  return fs
    .readdirSync(GOLDEN_DIR)
    .filter(f => f.endsWith('.json'))
    .map(f => f.replace(/\.json$/, ''))
    .sort()
}

/**
 * Asserts a JS-compiled workflow matches its Java golden, with a readable
 * diff on mismatch: compares the JSON forms first (good jest diff), then
 * proto message equality as the authoritative check.
 */
export function expectMatchesGolden(compiled: PutWfSpecRequest, goldenName: string): void {
  const golden = loadGolden(goldenName)
  expect(PutWfSpecRequest.toJson(compiled)).toEqual(PutWfSpecRequest.toJson(golden))
  expect(PutWfSpecRequest.equals(compiled, golden)).toBe(true)
}
