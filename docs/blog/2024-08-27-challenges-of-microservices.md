---
slug: challenge-of-microservices
title: The Challenge of Microservices
authors:
  name: Colt McNealy
  title: Managing Member of the LLC
  url: https://www.linkedin.com/in/colt-mcnealy-900b7a148/
  image_url: https://avatars.githubusercontent.com/u/100447728
tags: [tech-trends,microservices,littlehorse]
---

<!-- ---
Benefits of Microservices
- Different dev teams can work independently in parallel
- Bounded Contexts can theoretically ensure that smaller teams
- Faster deployment and iteration
- Take advantage of cloud native systems -->

Microservices are often necessary, but unfortunately they bring with them some baggage. <!-- truncate -->

Last week, I [blogged](./2024-08-22-promise-of-microservices.md) about the problems that microservices solve, and why they are not only beneficial but necessary in some cases (a good bellwether is the size of your engineering team: beyond 1 or 2 dozen engineers, you will probably start to feel some problems that can be solved with microservices).

When done correctly, microservices remove several bottlenecks to scaling your business. However, even well-architected microservices bring significant _accidental complexity_.

:::info
This is the second part of a 3-part blog series:

1. [The Promise of Microservices](./2024-08-22-promise-of-microservices.md)
2. **[This Post]** The Challenge with Microservices
3. **[Coming Soon]** Workflow and Microservices: A Match Made in Heaven
:::

In particular, microservices are:

1. Harder to **observe** and debug.
2. Harder to make **reliable** in the case of infrastructure or sofware failures.
3. More complex to **maintain** and evolve with changing business practices.

In this article we will explore how the above problems arise from two key facts:
* Microservices are **distributed**.
* Microservices are **choreographed without a leader**.

:::note
Microservices bring with them additional challenges around operationalization and deployment. However, those challenges are out-of-scope for this blog post as we instead choose to focus on the challenges faced by _application development teams_ rather than operations teams.
:::

## The Nature of Microservices

### Microservices are Distributed

### Microservices are Leaderless

## Distributed Observability

I would be first to admit that microservices bring with them a series of headaches around cost, observability, maintenance, and ease of evolution (otherwise, I would not have founded LittleHorse Enterprises!). However, microservice architecture plays a vital role in addressing two critical trends reshaping the software development landscape today:

* Increased digitization of companies in all business sectors (accelerated by the rise of AI).
* Elasticity of cloud computing.

### Increased Digitization

The level of digitization expected of businesses in order to compete in the modern market has drastically increased: IT teams must build software that interfaces with an ever-expanding list of external API's, legacy systems, user interfaces, internal tools, and SaaS providers.

For example: in the early 2000's, it was perfectly acceptable (even _expected_) for a passenger to book airline tickets over the telephone or through a travel agency. However, such an experience would be unheard of today and would immediately hobble an airline who provided such poor digital services.

In addition to using automation to provide better customer services, companies are generating, processing, and analyzing massive amounts of data. For example, grocery stores with razor-thin margins analyze seasonal consumption patterns in order to optimize inventory and prevent costly food waste.

These trends have coincided with (or _caused_, I would argue) a proliferation in the number of 1) software developers, and 2) software tools and API's found within companies in all industries, leading to two new problems:

1. Allowing large teams of software developers to productively work on an enterprise application in parallel (without stepping on each others' toes).
2. Ensuring that business requirements are effectively communicated to the entire (larger) software engineering team.

### Cloud Elasticity

As the importance and quantity of digital software systems exploded over the last two decades, so has the availability of nearly-infinite compute power delivered through cloud infrastructure providers such as AWS.

The promise of _elasticity_, or the ability to quickly spin compute resources up or down according to load and only pay for what you use, is unique to the cloud: for on-prem datacenters, spinning up new compute means buying new machines from Sun Microsystems (hopefully not Microsoft!), and scaling down compute means trying to sell them off on the secondary market. (Ask my father about how that went for a lot of people in 2001.)

Beyond scaling up and down, elasticity enables different deployment patterns that did not exist before. Whereas pre-cloud enterprises had dedicated and centralized data-center teams who were in charge of running applications, the accessibility of cloud computing gave rise to the DevOps movement. This has empowered smaller teams of software developers to take on the task of transferring software from "it works on my laptop!" to "it's now deployed in production!"

## Why Microservices?

Despite the extra complexity it brings, the microservice architecture can more than pay for itself by ensuring organizational alignment and allowing enterprise architectures to take full advantage of the cloud's elasticity.

### Organizational Alignment

As discussed earlier, the business problems that software engineering organizations must solve today dwarf those that were solved in the 1990's, and so do the software engineering teams that tackle those problems.

:::note
I am not belittling the engineers of the 90's; the problems they solved were arguably _much harder_ than the problems we face today, and there were fewer engineers to face those problems. However, it is a fact that users expect more digital-native experiences today than they did twenty years ago.
:::

By breaking applications into smaller services, we can accomplish several important things:
* Break up our software engineering team into smaller teams which are each responsible for individual microservices.
* Allow different components of a system to be developed with separate tech stacks and released independently.

Engineering teams of over a few dozen engineers working on the same deployable piece of software is a recipe for inefficiency. Merge conflicts, arguments over tech stack, slow "release trains," and excessive intra-team coordination are just a few problems that arise. However, by breaking your application into smaller microservices, you can also break up your engineering organization into smaller, more efficient teams each in charge of a small number (prefably one!) of microservices.

As an added benefit, properly-designed microservice architectures can follow the principles of Domain Driven Design. Ideally, a single microservice corresponds to a _Bounded Context_ inside the business. This enables a small piece of the technical platform (a microservice) to be managed by a small team of software engineers, who collaborate closely with subject-matter experts and business stakeholders within a very specific domain of the business. Such close collaboration can foster better alignment between business goals and the software produced by engineering teams.

### Moving Faster

Microservices can allow developers to move faster by enabling continuous delivery and independent deployment of services. In a monolithic architecture, releasing a new feature or fixing a bug typically requires redeploying the entire application. Since microservices allow smaller pieces of your application to be deployed independently, engineering teams can iterate faster and deliver incremental value to business stakeholders.

These positive effects are amplified by the advent of cloud computing. Since deploying a new application no longer requires buying a physical machine and plugging it into your datacenter but rather just applying a new `Deployment` and `Service` on a Kubernetes cluster, it is now truly feasible for small teams of software engineers to own their application stack from laptop-to-production (obviously, within the guardrails set by the central platform team). Furthermore, cloud computing is a pay-as-you-go (and often even pay-for-what-you-use) expense rather than an up-front cost. Therefore, the dollar cost of infrastructure required to support microservices is much lower today than it would have been before the advent of cloud computing and kubernetes.

## Conclusion

The microservice architecture is not just a Twitter-driven buzzword but rather a way of designing system that has several real advantages. For most organizations with over two dozen software engineers, building applications with microservices is not an option but rather a _necessity_. However, those advantages come with a cost.

We will discuss those challenges in next week's blog post...in the meantime, though, join our [Community Slack](https://launchpass.com/littlehorsecommunity) to get the latest updates!
