# Phase 1: Requirements Definition

## Project Overview

ASTIS is an AI-powered study and task intelligence system designed to help students manage academic tasks, track study behaviour, and receive intelligent task prioritisation recommendations.

The project aims to transform a traditional task list into a data-driven productivity assistant by combining task management, behaviour logging, feature extraction, and an explainable AI recommendation model.

## Target Users

The target users are students who regularly manage multiple academic responsibilities, including:

- Coursework and assignments
- Exam preparation
- Group projects
- Personal study plans
- Overlapping deadlines

## Problem Statement

Students often struggle to manage academic workload because they need to handle multiple deadlines, estimate task importance, and maintain consistent productivity over time.

Traditional to-do list tools can record tasks, but they usually do not learn from user behaviour or provide personalised guidance. As a result, students may still find it difficult to decide which task should be completed first.

ASTIS aims to solve this problem by building a system that records tasks, tracks user behaviour, extracts behavioural features, predicts delay risk, and generates task priority recommendations based on urgency, user-defined priority, and historical completion patterns.

## Core Problems

- Students may not know which task should be completed first.
- Overlapping deadlines can make workload management difficult.
- Users often lack insight into their delay patterns and study behaviour.
- Static task lists do not adapt to user behaviour.

## MVP Features

### Core Features

- User registration and login
- User profile management for personalisation
- Task creation, reading, updating, and deletion
- Deadline and task status tracking
- User-defined task priority
- Behaviour log recording for task actions
- Feature extraction from tasks and behaviour logs
- AI-based priority scoring and delay risk prediction
- Recommended task ordering with score explanations

### Extended Features

- Dashboard visualisation
- Completion rate analysis
- Delay pattern analysis
- Personalised study schedule generation
- Advanced machine learning prediction model
- Cloud deployment
- Redis caching for performance optimisation

## Non-Functional Requirements

### Security

- User authentication should be implemented using JWT.
- Passwords should be securely hashed.
- Users should only access their own tasks and behaviour records.

### Performance

- Common API responses should be returned within an acceptable response time.
- Task recommendation should be generated quickly for normal personal workloads.

### Scalability

- The backend should use a modular structure.
- The AI recommendation logic should be separated from core task management logic.
- The system should allow future extension to a more advanced machine learning model.

### Maintainability

- The backend should follow layered architecture.
- Controller, service, repository, and model layers should be clearly separated.
- DTOs should be used to separate API contracts from database entities.
- Validation and global exception handling should be applied consistently.
- API documentation should be provided using Swagger or OpenAPI.

## Success Criteria

The first version of ASTIS will be considered successful if:

- A user can register and log in.
- A user can manage tasks through REST APIs.
- The system can store task and behaviour data.
- The system can extract features such as urgency and historical completion behaviour.
- The system can generate a ranked task recommendation list with priority score and delay risk information.
- The project structure supports future extension of analytics and AI modules.

## Initial Scope Limitation

The MVP will not include a complex deep learning model.

The first version will use an embedded, deterministic AI scoring model inside the backend service layer. It will not rely on a ChatGPT-style suggestion wrapper.

The model will be based on:

- Deadline urgency
- User-defined priority
- Historical completion behaviour
- Delay risk
- Time decay factor
- Task type or category behaviour

Advanced machine learning models may be added in later iterations.
