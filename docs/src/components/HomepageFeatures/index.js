import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Reliability',
    Svg: require('@site/static/img/reliability.svg').default,
    description: (
      <>
        Make your apps resilient to infrastructure failures and network outages.
      </>
    ),
  },
  {
    title: 'Observability',
    Svg: require('@site/static/img/observability.svg').default,
    description: (
      <>
        No more hours of <code>grep</code>ing through logs to find a bug.
      </>
    ),
  },
  {
    title: 'Scalability',
    Svg: require('@site/static/img/scalability.svg').default,
    description: (
      <>
        Scale to dozens of thousands of <code>TaskRun</code>s per second, with latency as low as 20ms.
      </>
    ),
  },
  {
    title: 'Composability',
    Svg: require('@site/static/img/composability.svg').default,
    description: (
      <>
        Build a library of modular Task Workers which you can re-use across all of your workflows.
      </>
    ),
  },
  {
    title: 'Velocity',
    Svg: require('@site/static/img/velocity.svg').default,
    description: (
      <>
        Develop new features or change existing business workflows with ease.
      </>
    ),
  },
  {
    title: 'Reactivity',
    Svg: require('@site/static/img/reactivity.svg').default,
    description: (
      <>
        Write Workflows which seamlessly integrate with and react to external systems.
      </>
    ),
  },
];

function Feature({Svg, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        <Svg className={styles.featureSvg} role="img" />
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
