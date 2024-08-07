# TimelineParser CLI

`TimelineParser` is a command-line interface (CLI) tool for generating metrics and parsing timeline data from Develocity Timeline section available at `scan-data/gradle/$BUILD_SCAN_ID/timeline`.

## Usage

### Download the CLI
```sh

 curl -L https://github.com/cdsap/TimelineParser/releases/download/v0.1.1/timelineparser --output timelineparser
 chmod 0757 timelineparser

# Generate Metrics from two timeline json files
./timelineparser --mode generate-metrics  --first-timeline first.json --second-timeline second.json

# Generate Metrics from two timeline json files generating a Trace event file
./timelineparser --mode generate-metrics  --first-timeline first.json --second-timeline second.json --generate-trace-events

# Generate Build Models from timeline json file
./timelineparser -mode generate-models --timelie timeline1.json

```
### From sources
```sh

./gradlew :fatBinary

./timelineparer -mode generate-metrics  --first-timeline first.json --second-timeline second.json
```


### Modes

1. **generate-metrics**: Compare two timeline files and generate metrics using `io.github.cdsap:comparescans`
2. **generate-models**: Generate models from the timeline files based on the `Build` entity of `io.github.cdsap:geapi-data`

### Options

- `--mode` (required): Specifies the mode of operation. Possible values are `generate-metrics` and `genrate-models`.
- `--first-timeline`: The first `timeline.json` file from Develocity for generating metrics (required for `generate-metrics` mode).
- `--second-timeline`: The second `timeline.json` file from Develocity for generating metrics (required for `generate-metrics` mode).
- `--timeline`: The `timeline.json` file(s) from Develocity for parsing (required for `parse` mode, can be specified multiple times).
- `--generate-trace-events`: Flag to generate trace event files (optional).

## Analyzing data
The metrics generated by the CLI can be used to analyze the performance of the build. You have an example of the metrics generated by the CLI in the following [example](resources/metrics-timeline1-timeline2.csv).
You can use the [CompareScans](https://github.com/cdsap/CompareScans) tool to apply rules and generate insights from the metrics.

## Notes
* There is no guarantee that the timeline model will not change. Users should be aware that future updates to the timeline structure may require adjustments to the parser.
* The cache size artifact is rounded by the qualifier.


