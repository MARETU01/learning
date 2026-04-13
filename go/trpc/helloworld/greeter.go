package main

import (
	"context"

	pb "git.woa.com/trpcprotocol/test/helloworld"
)

type greeterImpl struct {
	pb.UnimplementedGreeter
}

func (s *greeterImpl) SayHello(
	ctx context.Context,
	req *pb.HelloRequest,
) (*pb.HelloReply, error) {
	rsp := &pb.HelloReply{}
	rsp.Msg = "Received message: " + req.Msg + " Hello from trpc-go!"
	return rsp, nil
}
