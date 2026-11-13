use littlehorse::{proto::*, *};
struct Mulberry32(u32);
impl Mulberry32 {
    fn next(&mut self, bound: u32) -> u32 {
        self.0 = self.0.wrapping_add(0x6d2b79f5);
        let mut value = (self.0 ^ (self.0 >> 15)).wrapping_mul(1 | self.0);
        value = value.wrapping_add((value ^ (value >> 7)).wrapping_mul(61 | value)) ^ value;
        (value ^ (value >> 14)) % bound
    }
}
pub fn build(seed: u32, operations: u32) -> Result<Workflow, WorkflowError> {
    let mut random = Mulberry32(seed);
    Workflow::define(format!("fuzz-{seed}"), |thread| {
        let mut integers = Vec::new();
        for index in 0..operations {
            match random.next(8) {
                0 => integers.push(thread.declare_int(format!("v{index}"))),
                1 => {
                    thread.declare_str(format!("v{index}"));
                }
                2 => {
                    thread.declare_bool(format!("v{index}"));
                }
                3 => {
                    thread.execute(format!("task-{}", random.next(5)), []);
                }
                4 => {
                    thread.sleep(i64::from(1 + random.next(60)).into());
                }
                5 => {
                    thread.wait_for_event(EventOptions::new(format!("evt-{}", random.next(5))));
                }
                6 if integers.is_empty() => integers.push(thread.declare_int(format!("v{index}"))),
                6 => {
                    let variable = &integers[random.next(integers.len() as u32) as usize];
                    thread.mutate(
                        variable,
                        VariableMutationType::Add,
                        i64::from(random.next(10)).into(),
                    );
                }
                _ if integers.is_empty() => {
                    thread.execute(format!("task-{}", random.next(5)), []);
                }
                _ => {
                    let variable = &integers[random.next(integers.len() as u32) as usize];
                    let rhs = i64::from(random.next(10));
                    let task = format!("branch-{}", random.next(5));
                    thread.do_if(variable.expression().greater_than(rhs), |thread| {
                        thread.execute(task, []);
                        Ok(())
                    })?;
                }
            }
        }
        Ok(())
    })
}
