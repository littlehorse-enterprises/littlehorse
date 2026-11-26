//! Bounded, asynchronous cursor pagination without collecting an unbounded result set.
use std::future::Future;
#[derive(Clone, Debug)]
pub struct Page<T> {
    pub values: Vec<T>,
    pub bookmark: Option<Vec<u8>>,
}
pub struct Paginator<T, F> {
    fetch: F,
    bookmark: Option<Vec<u8>>,
    done: bool,
    marker: std::marker::PhantomData<T>,
}
impl<T, F, Fut> Paginator<T, F>
where
    F: FnMut(Option<Vec<u8>>) -> Fut,
    Fut: Future<Output = Result<Page<T>, tonic::Status>>,
{
    pub fn new(fetch: F) -> Self {
        Self {
            fetch,
            bookmark: None,
            done: false,
            marker: std::marker::PhantomData,
        }
    }
    /// Fetch exactly one page. The closure copies the cursor to its search/list request.
    pub async fn next_page(&mut self) -> Result<Option<Vec<T>>, tonic::Status> {
        if self.done {
            return Ok(None);
        }
        let page = (self.fetch)(self.bookmark.clone()).await?;
        if page.bookmark.is_some() && page.bookmark == self.bookmark {
            return Err(tonic::Status::failed_precondition(
                "server repeated its pagination bookmark",
            ));
        }
        self.bookmark = page.bookmark;
        self.done = self.bookmark.is_none();
        Ok(Some(page.values))
    }
}
