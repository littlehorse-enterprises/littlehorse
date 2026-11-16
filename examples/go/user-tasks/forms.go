package usertasks

import "github.com/littlehorse-enterprises/littlehorse/sdk-go/littlehorse"

type ItemRequestForm struct {
	RequestedItem string `lh:"requestedItem" lhdesc:"The item you are requesting."`
	Justification string `lh:"justification" lhdesc:"Why you need this request."`
}

func (ItemRequestForm) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "item-request-form"}
}

type ApprovalForm struct {
	IsApproved bool `lh:"isApproved" lhdesc:"Reply 'true' if this is an acceptable request."`
}

func (ApprovalForm) LHStructDef() littlehorse.LHStructDefInfo {
	return littlehorse.LHStructDefInfo{Name: "approval-form"}
}
