"use server";
import { lhClient } from "@/lib/lhClient";
import { WithTenant } from "@/types";
import { SearchWfRunRequest, WfRunIdList } from "littlehorse-client/proto";

export interface PaginatedWfRunIdList extends WfRunIdList {
  bookmarkAsString: string | undefined;
}

type WithBookmarkAsString = {
  bookmarkAsString: string | undefined;
};

export type WfRunSearchProps = SearchWfRunRequest &
  WithTenant &
  WithBookmarkAsString;
export const searchWfRun = async ({
  tenantId,
  bookmarkAsString,
  ...req
}: WfRunSearchProps): Promise<PaginatedWfRunIdList> => {
  const client = await lhClient({ tenantId });
  const requestWithBookmark = bookmarkAsString
    ? { ...req, bookmark: Buffer.from(bookmarkAsString, "base64") }
    : req;
  const wfRunIdList = await client.searchWfRun(requestWithBookmark);

  return {
    ...wfRunIdList,
    bookmarkAsString: wfRunIdList.bookmark?.toString("base64"),
  };
};
