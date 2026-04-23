<p>
  <img src="docs/assets/HiPUTS_Logo.png" alt="HiPUTS logo" width="720">
</p>

# HiPUTS


**HiPUTS** (High Performance Urban Traffic Simulator) is a research-oriented urban traffic simulator with continuous decision model and continuous space model.

The project focuses on **microscopic**, **continuous**, and **distributed** traffic simulation. Its architecture is designed for large-scale execution, where the road network is partitioned into patches and processed by multiple workers in a decentralized way.

## What HiPUTS is

HiPUTS was created as a proof-of-concept platform for executing large-scale urban traffic simulations in distributed high-performance environments. The core design goal is not only to simulate traffic with high fidelity, but also to preserve correctness and consistency when the simulation is executed across many workers.

In practical terms, HiPUTS combines:

- a **continuous microscopic traffic model**,
- **distributed execution** across multiple worker processes,
- **patch-based partitioning** of the road network,
- **peer-to-peer synchronization** during runtime,
- and **runtime load balancing** for handling uneven traffic density.

## Main characteristics

- **Microscopic and continuous model** – vehicles and infrastructure are represented in a detailed way rather than as coarse aggregates.
- **Distributed-first design** – the simulator is intended to run as multiple cooperating worker processes.
- **Initialization server only at startup** – one process temporarily coordinates initialization, but after startup the simulation continues in a decentralized worker-to-worker mode.
- **Patch-based decomposition** – the road network is partitioned into patches assigned to workers.
- **Scale-oriented architecture** – the design aims at distribution transparency, bounded communication, and scalable execution.

## How distributed startup works

HiPUTS is a **distributed simulator**, so the number of running worker processes matters.

At startup:

1. The first launched process on a machine creates a file named `serverLock.txt`.
2. The existence of this file is treated as information that the initialization server is already running.
3. If the file does **not** exist, that process becomes the temporary **initialization server**.
4. If the file **does** exist, the next launched process starts as a normal **worker**.
5. Workers connect to the server socket using the configured server endpoint (`serverAddress` / `serverPort`, local or remote).
6. After initialization is complete, the server also becomes a normal worker and the simulation continues in a fully distributed, peer-to-peer mode.

This means the server is used only for startup and initial coordination. During simulation runtime, communication is performed directly between workers.

**Warning:** Normally this file is removed after simulation is finished and the most cases of closing the processes are handled, but if for some reason you would encounter that when running simulation causes workers not to span server first double check if this file is **removed** before the launch.


## Configuration

HiPUTS behavior is controlled through runtime configuration.

In the current repository layout, the default values are defined in:

```text
src/main/resources/application.yml
```

If your deployment uses an external `settings.json`, the same runtime parameters should be configured there.

The most important parameters for getting started are:

- `workerCount` – number of worker processes that should participate in the distributed simulation,
- `coresPerWorkerCount` – number of threads used inside a worker,
- `mapPath` – path to the input map,
- `readFromOsmDirectly` – whether the map should be read directly from an OSM file,
- `serverAddress` and `serverPort` – endpoint used by workers to reach the initialization server,
- `enableGUI` – whether each worker should open the local GUI,
- `enableVisualization` – whether Kafka-based visualization is enabled,
- `simulationStep` and `simulationTimeStep` – main simulation time controls,
- `balancingMode` – load-balancing strategy.

To change the number of distributed workers, update:

```text
workerCount
```

The full description of configuration parameters is available in the `Configuration` class.

> In the current source tree this class is located at `pl.edu.agh.hiputs.model.Configuration`.

## How to run

### 1. Build the project

Use the Gradle wrapper included in the repository:

```bash
./gradlew clean bootJar
```

This produces:

```text
build/libs/HiPUTS.jar
```

### 2. Prepare configuration

Before running the simulator, review the runtime settings `settings.json` file.


Typical first changes:

- set `workerCount` to the number of worker processes you want to run,
- set `mapPath` to the input map,
- keep `readFromOsmDirectly=true` when using an OSM file,
- disable GUI on headless environments by setting `enableGUI=false`,
- verify `serverAddress` and `serverPort` for local or remote deployments.

### 3. Start the distributed simulation

Launch the application **exactly as many times as specified by `workerCount`**.

For example, if `workerCount=2`, start two processes:

```bash
java -jar build/libs/HiPUTS.jar
java -jar build/libs/HiPUTS.jar
```

The first process will become the temporary initialization server (creating `serverLock.txt`), and the other process will start as a worker.

For more workers, start more processes:

```bash
java -jar build/libs/HiPUTS.jar
java -jar build/libs/HiPUTS.jar
java -jar build/libs/HiPUTS.jar
java -jar build/libs/HiPUTS.jar
```

### 4. Remote deployment

For multi-machine deployments:

- start one process on the machine that should act as the initialization server,
- configure the remaining processes so they can reach that machine using `serverAddress` and `serverPort`,
- launch the remaining worker processes on local or remote hosts.

After startup, runtime synchronization and data exchange happen directly between workers.

## Input data

HiPUTS supports two ways of loading the road network:

1. **Directly from an OpenStreetMap file**
    - set `mapPath` to an `.osm` file,
    - set `readFromOsmDirectly=true`.

2. **From a preprocessed import package**
    - set `mapPath` to a directory containing processed map files such as `edges.csv`, `nodes.csv`, and `patches.csv`,
    - set `readFromOsmDirectly=false`.

The repository includes example input data in the `data/` directory.

For execution:

- `simulationStep` defines the number of simulation steps,
- `workerCount` defines the number of worker processes used in distributed mode,
- the number of launched processes should match `workerCount`.

## Logs

When HiPUTS starts, it creates a `logs/` directory in the directory from which the application was launched.

Log output is written into separate files for each started worker process. One of these files also contains the logs produced by the temporary initialization server, because that process later continues as a normal worker after startup is complete.

This means that for a distributed run you should expect multiple log files in the `logs/` directory, typically one per launched process.

## Statistics output

HiPUTS also creates a `statistic/` directory in the directory from which the application was launched.

This directory contains CSV files with runtime statistics collected during the simulation. Typical files include:

- `car.csv` – number of cars assigned to each worker at each simulation step,
- `workerCost.csv` – per-step worker cost/load values used to observe load distribution,
- `iterationData.csv` – per-worker, per-step operational data such as number of cars, stopped cars, message counts, message sizes, and memory usage,
- `iterationTimes.csv` – detailed per-worker timing breakdown of simulation stages for each step,
- `messagesCount.csv` – counts of exchanged messages by message type,
- `messagesSizes.csv` – total transferred sizes of exchanged messages by message type,
- `patchExchanges.csv` – information about patch transfers between workers,
- `summaryTimes.csv` – aggregated timing summaries for the server and workers.

These files are useful for evaluating simulation behavior, communication overhead, load balancing, and execution time in distributed runs.


## Notes on execution

- `workerCount` refers to the distributed worker setup, so you should launch that many processes.
- `coresPerWorkerCount` controls parallelism inside each worker process.
- If `enableGUI=true`, each worker may open its own GUI window.
- If `enableVisualization=true`, additional visualization infrastructure may be required.
- During startup, the initialization server partitions the map, assigns patches, informs workers about neighbors, and then joins the simulation as a normal worker.

## Project background

This repository contains the implementation accompanying the research on super-scalable urban traffic simulation. The simulator was designed to demonstrate that a continuous microscopic traffic model can be executed in a distributed environment while preserving correctness and achieving strong scalability properties.

## Suggested citation

If you reference HiPUTS in academic work, please cite the related conference paper:

BibTeX:
```bibtex
@inproceedings{najdek2025hiputs,
  title={Hiputs: Super-scalable simulation of microscopic continuous urban traffic model},
  author={Najdek, Mateusz and Brzozowska, Natalia and Turek, Wojciech},
  booktitle={Proceedings of the 39th ECMS International Conference on Modelling and Simulation},
  pages={476--485},
  year={2025}
}
```


