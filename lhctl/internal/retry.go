package internal

import (
	"context"
	"time"

	"google.golang.org/genproto/googleapis/rpc/errdetails"
	"google.golang.org/grpc"
	"google.golang.org/grpc/codes"
	"google.golang.org/grpc/status"
)

func resourceExhaustedRetryInterceptor() grpc.UnaryClientInterceptor {
	return func(
		ctx context.Context,
		method string,
		req interface{},
		reply interface{},
		cc *grpc.ClientConn,
		invoker grpc.UnaryInvoker,
		opts ...grpc.CallOption,
	) error {
		for {
			err := invoker(ctx, method, req, reply, cc, opts...)
			if err == nil {
				return nil
			}

			delay, ok := retryDelayFromError(err)
			if !ok {
				return err
			}

			if err := sleepWithContext(ctx, delay); err != nil {
				return status.FromContextError(err).Err()
			}
		}
	}
}

func retryDelayFromError(err error) (time.Duration, bool) {
	grpcStatus, ok := status.FromError(err)
	if !ok || grpcStatus.Code() != codes.ResourceExhausted {
		return 0, false
	}

	for _, detail := range grpcStatus.Details() {
		retryInfo, ok := detail.(*errdetails.RetryInfo)
		if !ok || retryInfo.GetRetryDelay() == nil {
			continue
		}

		delay := retryInfo.GetRetryDelay().AsDuration()
		if delay <= 0 {
			return 0, false
		}
		return delay, true
	}

	return 0, false
}

func sleepWithContext(ctx context.Context, delay time.Duration) error {
	timer := time.NewTimer(delay)
	defer timer.Stop()

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-timer.C:
		return nil
	}
}
