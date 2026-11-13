# SDK Conformance Matrix

## wfsdk

| case | level | java | js | rust |
| --- | --- | --- | --- | --- |
| add-variable | required | ✅ pass | ✅ pass | ✅ pass |
| assign-user-task | required | ✅ pass | ✅ pass | ✅ pass |
| build-inline-struct | required | ✅ pass | ✅ pass | ✅ pass |
| build-struct | required | ✅ pass | ✅ pass | ✅ pass |
| cancel-user-task-after-assignment | required | ✅ pass | ✅ pass | ✅ pass |
| cancel-user-task-run-after | required | ✅ pass | ✅ pass | ✅ pass |
| declare-array | required | ✅ pass | ✅ pass | ✅ pass |
| declare-bool | required | ✅ pass | ✅ pass | ✅ pass |
| declare-bytes | required | ✅ pass | ✅ pass | ✅ pass |
| declare-double | required | ✅ pass | ✅ pass | ✅ pass |
| declare-int | required | ✅ pass | ✅ pass | ✅ pass |
| declare-json-arr | required | ✅ pass | ✅ pass | ✅ pass |
| declare-json-obj | required | ✅ pass | ✅ pass | ✅ pass |
| declare-map | required | ✅ pass | ✅ pass | ✅ pass |
| declare-str | required | ✅ pass | ✅ pass | ✅ pass |
| declare-struct | required | ✅ pass | ✅ pass | ✅ pass |
| declare-timestamp | required | ✅ pass | ✅ pass | ✅ pass |
| do-else | required | ✅ pass | ✅ pass | ✅ pass |
| do-else-if | required | ✅ pass | ✅ pass | ✅ pass |
| do-if | required | ✅ pass | ✅ pass | ✅ pass |
| do-if-else | required | ✅ pass | ✅ pass | ✅ pass |
| do-while | required | ✅ pass | ✅ pass | ✅ pass |
| execute-args | required | ✅ pass | ✅ pass | ✅ pass |
| expr-add | required | ✅ pass | ✅ pass | ✅ pass |
| expr-and | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-bool | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-bytes | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-double | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-int | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-str | required | ✅ pass | ✅ pass | ✅ pass |
| expr-cast-to-wf-run-id | required | ✅ pass | ✅ pass | ✅ pass |
| expr-divide | required | ✅ pass | ✅ pass | ✅ pass |
| expr-does-contain | required | ✅ pass | ✅ pass | ✅ pass |
| expr-does-not-contain | required | ✅ pass | ✅ pass | ✅ pass |
| expr-extend | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-equal-to | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-greater-than | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-in | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-less-than | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-not-equal-to | required | ✅ pass | ✅ pass | ✅ pass |
| expr-is-not-in | required | ✅ pass | ✅ pass | ✅ pass |
| expr-multiply | required | ✅ pass | ✅ pass | ✅ pass |
| expr-or | required | ✅ pass | ✅ pass | ✅ pass |
| expr-pow | required | ✅ pass | ✅ pass | ✅ pass |
| expr-remove-if-present | required | ✅ pass | ✅ pass | ✅ pass |
| expr-remove-index | required | ✅ pass | ✅ pass | ✅ pass |
| expr-remove-key | required | ✅ pass | ✅ pass | ✅ pass |
| expr-size | required | ✅ pass | ✅ pass | ✅ pass |
| expr-subtract | required | ✅ pass | ✅ pass | ✅ pass |
| external-event-correlation | required | ✅ pass | ✅ pass | ✅ pass |
| external-event-timeout | required | ✅ pass | ✅ pass | ✅ pass |
| fail | required | ✅ pass | ✅ pass | ✅ pass |
| format | required | ✅ pass | ✅ pass | ✅ pass |
| handle-any-failure | required | ✅ pass | ✅ pass | ✅ pass |
| handle-error-any | required | ✅ pass | ✅ pass | ✅ pass |
| handle-exception-named | required | ✅ pass | ✅ pass | ✅ pass |
| interrupt-handler | required | ✅ pass | ✅ pass | ✅ pass |
| mutate | required | ✅ pass | ✅ pass | ✅ pass |
| node-output-get | required | ✅ pass | ✅ pass | ✅ pass |
| node-output-json-path | required | ✅ pass | ✅ pass | ✅ pass |
| reassign-user-task | required | ✅ pass | ✅ pass | ✅ pass |
| release-to-group-on-deadline | required | ✅ pass | ✅ pass | ✅ pass |
| run-wf-inputs | required | ✅ pass | ✅ pass | ✅ pass |
| schedule-reminder-on-assignment | required | ✅ pass | ✅ pass | ✅ pass |
| schedule-reminder-task | required | ✅ pass | ✅ pass | ✅ pass |
| sleep-seconds | required | ✅ pass | ✅ pass | ✅ pass |
| sleep-until | required | ✅ pass | ✅ pass | ✅ pass |
| spawn-thread-for-each | required | ✅ pass | ✅ pass | ✅ pass |
| spawn-thread-input-vars | required | ✅ pass | ✅ pass | ✅ pass |
| spawned-thread-number | required | ✅ pass | ✅ pass | ✅ pass |
| spawned-threads-of | required | ✅ pass | ✅ pass | ✅ pass |
| task-exponential-backoff | required | ✅ pass | ✅ pass | ✅ pass |
| task-timeout | required | ✅ pass | ✅ pass | ✅ pass |
| task-with-retries | required | ✅ pass | ✅ pass | ✅ pass |
| throw-event | required | ✅ pass | ✅ pass | ✅ pass |
| user-task-notes | required | ✅ pass | ✅ pass | ✅ pass |
| user-task-on-cancellation-exception | required | ✅ pass | ✅ pass | ✅ pass |
| var-as-inherited | required | ✅ pass | ✅ pass | ✅ pass |
| var-as-public | required | ✅ pass | ✅ pass | ✅ pass |
| var-assign | required | ✅ pass | ✅ pass | ✅ pass |
| var-get-field | required | ✅ pass | ✅ pass | ✅ pass |
| var-is-greater-than-eq | required | ✅ pass | ✅ pass | ✅ pass |
| var-is-less-than-eq | required | ✅ pass | ✅ pass | ✅ pass |
| var-json-path | required | ✅ pass | ✅ pass | ✅ pass |
| var-masked | required | ✅ pass | ✅ pass | ✅ pass |
| var-required | required | ✅ pass | ✅ pass | ✅ pass |
| var-searchable | required | ✅ pass | ✅ pass | ✅ pass |
| var-searchable-on | required | ✅ pass | ✅ pass | ✅ pass |
| var-with-access-level | required | ✅ pass | ✅ pass | ✅ pass |
| var-with-default | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-any-of | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-child-wf | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-condition | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-event | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-first-of | required | ✅ pass | ✅ pass | ✅ pass |
| wait-for-threads | required | ✅ pass | ✅ pass | ✅ pass |
| wait-threads-handle-any-failure-on-child | required | ✅ pass | ✅ pass | ✅ pass |
| wait-threads-handle-error-on-child | required | ✅ pass | ✅ pass | ✅ pass |
| wait-threads-handle-exception-on-child | required | ✅ pass | ✅ pass | ✅ pass |
| wf-default-task-backoff | required | ✅ pass | ✅ pass | ✅ pass |
| wf-default-task-retries | required | ✅ pass | ✅ pass | ✅ pass |
| wf-default-task-timeout | required | ✅ pass | ✅ pass | ✅ pass |
| wf-default-thread-retention | required | ✅ pass | ✅ pass | ✅ pass |
| wf-retention-policy | required | ✅ pass | ✅ pass | ✅ pass |
| wf-set-parent | required | ✅ pass | ✅ pass | ✅ pass |
| wf-update-type | required | ✅ pass | ✅ pass | ✅ pass |
| workflow-minimal | required | ✅ pass | ✅ pass | ✅ pass |
| wt-add | required | ✅ pass | ✅ pass | ✅ pass |
| wt-complete | required | ✅ pass | ✅ pass | ✅ pass |
| wt-divide | required | ✅ pass | ✅ pass | ✅ pass |
| wt-extend | required | ✅ pass | ✅ pass | ✅ pass |
| wt-multiply | required | ✅ pass | ✅ pass | ✅ pass |
| wt-pow | required | ✅ pass | ✅ pass | ✅ pass |
| wt-remove-if-present | required | ✅ pass | ✅ pass | ✅ pass |
| wt-remove-index | required | ✅ pass | ✅ pass | ✅ pass |
| wt-remove-key | required | ✅ pass | ✅ pass | ✅ pass |
| wt-retention-policy | required | ✅ pass | ✅ pass | ✅ pass |
| wt-subtract | required | ✅ pass | ✅ pass | ✅ pass |

**java**: 119/119 pass · **js**: 119/119 pass · **rust**: 119/119 pass

## serde

| case | level | java | js | rust |
| --- | --- | --- | --- | --- |
| str-basic | required | ✅ pass | ✅ pass | ✅ pass |
| str-empty | required | ✅ pass | ✅ pass | ✅ pass |
| str-unicode | required | ✅ pass | ✅ pass | ✅ pass |
| int-answer | required | ✅ pass | ✅ pass | ✅ pass |
| int-zero | required | ✅ pass | ✅ pass | ✅ pass |
| int-negative | required | ✅ pass | ✅ pass | ✅ pass |
| int-max-safe | required | ✅ pass | ✅ pass | ✅ pass |
| int-beyond-double | required | ✅ pass | ✅ pass | ✅ pass |
| double-half | required | ✅ pass | ✅ pass | ✅ pass |
| double-negative | required | ✅ pass | ✅ pass | ✅ pass |
| double-whole | required | ✅ pass | ✅ pass | ✅ pass |
| bool-true | required | ✅ pass | ✅ pass | ✅ pass |
| bool-false | required | ✅ pass | ✅ pass | ✅ pass |
| bytes-basic | required | ✅ pass | ✅ pass | ✅ pass |
| timestamp-millis | required | ✅ pass | ✅ pass | ✅ pass |
| json-obj-single | required | ✅ pass | ✅ pass | ✅ pass |
| json-obj-empty | required | ✅ pass | ✅ pass | ✅ pass |
| json-arr-single | required | ✅ pass | ✅ pass | ✅ pass |
| json-arr-empty | required | ✅ pass | ✅ pass | ✅ pass |
| null-value | required | ✅ pass | ✅ pass | ✅ pass |

**java**: 20/20 pass · **js**: 20/20 pass · **rust**: 20/20 pass

## registrations

| case | level | java | js | rust |
| --- | --- | --- | --- | --- |
| reg-external-event | required | ✅ pass | ✅ pass | ✅ pass |
| reg-correlated-event-config | required | ✅ pass | ✅ pass | ✅ pass |
| reg-workflow-event | required | ✅ pass | ✅ pass | ✅ pass |
| reg-interrupt-event-type | required | ✅ pass | ✅ pass | ✅ pass |
| req-task-def-names | required | ✅ pass | ✅ pass | ✅ pass |
| req-external-event-def-names | required | ✅ pass | ✅ pass | ✅ pass |
| req-child-wf-spec-names | required | ✅ pass | ✅ pass | ✅ pass |
| req-workflow-event-def-names | required | ✅ pass | ✅ pass | ✅ pass |

**java**: 8/8 pass · **js**: 8/8 pass · **rust**: 8/8 pass

**Overall** — **java**: 147/147 pass · **js**: 147/147 pass · **rust**: 147/147 pass

Generated by `sdk-conformance/runner/matrix.mjs` from `results/` at corpus revision `410fc56c0` — do not edit by hand.
