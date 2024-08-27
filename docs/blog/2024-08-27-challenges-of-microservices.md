---
slug: challenge-of-microservices
title: The Challenge of Microservices
authors:
- coltmcnealy
tags: [analysis]
---

Microservices are often necessary, but unfortunately they bring with them some baggage. <!-- truncate -->

:::info
This is the second part of a 3-part blog series:

1. [The Promise of Microservices](./2024-08-22-promise-of-microservices.md)
2. **[This Post]** The Challenge with Microservices
3. **[Coming Soon]** Workflow and Microservices: A Match Made in Heaven
:::

Last week, I [blogged](./2024-08-22-promise-of-microservices.md) about the problems that microservices solve, and why they are not only beneficial but necessary in some cases (a good bellwether is the size of your engineering team: beyond 1 or 2 dozen engineers, you will probably start to feel some problems that can be solved with microservices).

When done correctly, microservices remove several bottlenecks to scaling your business. However, even well-architected microservices bring significant _accidental complexity_.

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

As I described in [last week's blog](./2024-08-22-promise-of-microservices.md):

> The term "microservices" refers to a software architecture wherein an enterprise application comprises a collection of small, loosely coupled, and independently deployable services (these small services are called "microservices" in contrast to larger monoliths). Each microservice focuses on a specific business capability and communicates with other services over a network, typically through API's, streaming platforms, or message queues.

Crucially, a single microservice implements technical logic for a specific domain, or bounded context, within the larger company. In contrast, a comprehensive business process requires interacting with technology and people across _many_ business domains. The classic example of microservices architecture, e-commerce checkout, involves at least _shipping_, _billing_, _notifications_, _inventory_, and _orders_.

In the rest of this blog post we will examine microservices through the the lense of e-commerce checkout flow. To start with a simple use-case, the logical flow we will consider is:

1. When an order is placed, we create a record in a database in the `orders` service.
2. We then reserve inventory (and ensure that the item is in stock) in the `inventory` service.
3. We charge the customer using the `payments` service.
4. Lastly, we ship the item using the `shipping` service.



### Microservices are Distributed

### Microservices are Leaderless

## The Challenges

### Reliability and Correctness

### Observability

### Supporting New Business Workflows

## Looking Forward

Microservices have clear and proven benefits, and are often not just advantageous but _necessary_ in some cases. However, as we discussed today, those benefits do not come without a cost. Because microservices are inherently distributed systems, challenges such as reliability, observability, and coordination are exacerbated.

Without spoiling the punchline of the next blog post, these challenges are why I started LittleHorse almost three years ago. Stay tuned for a description of how a _workflow orchestrator_ can alleviate a good portion of the headaches that come along with microservices.

### Business Analytics

Astute readers may notice that when discussing the e-commerce checkout example, we didn't discuss the problem of _analytics._ We focused exclusively on online transaction processing, or ensuring that the orders are properly fulfilled and processed. However, no attention was paid to business analytics to optimize future sales!

This area is yet another challenge. The LittleHorse Council is working on a major feature (an output Kafka Topic with records for anytime something _interesting_ happens inside a `WfRun`) for the LH Server that will address this. Don't worry, we'll blog about it soon :wink:.
