import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'For Mission Critical Apps',
    Svg: require('@site/static/img/reliability.svg').default,
    description: (
      <>
        Scale to dozens of thousands of <code>TaskRun</code>s per second, with latency as low as 20ms, while
        making your apps resilient and observable.
      </>
    ),
  },
  {
    title: 'For Engineers, By Engineers',
    Svg: require('@site/static/img/observability.svg').default,
    description: (
      <>
        Libraries in Java, Go, and Python; source-available and free for production use.
        Designed with love from first principles.
      </>
    ),
  },
  {
    title: 'For Any Use Case',
    Svg: require('@site/static/img/reactivity.svg').default,
    description: (
      <>
        Fully-featured Workflow DSL that supports all programming primitives. Easy to
        learn and adopt without rewriting your existing apps.
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
