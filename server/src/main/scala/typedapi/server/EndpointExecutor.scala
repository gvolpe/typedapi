package typedapi.server

import shapeless._
import shapeless.ops.hlist.Prepend

sealed trait EndpointExecutor[R, El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut] {

  type Out

  def extract(eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, Fun, FOut]): Option[ROut] = 
    endpoint.extractor(eReq, Set.empty, HNil)

  def apply(req: R, eReq: EndpointRequest, endpoint: Endpoint[El, In, ROut, CIn, Fun, FOut]): Option[Out]
}

object EndpointExecutor {

  type Aux[R, El <: HList, In <: HList, ROut, CIn <: HList, Fun, FOut, Out0] = EndpointExecutor[R, El, In, ROut, CIn, Fun, FOut] {
    type Out = Out0
  }
}

trait NoReqBodyExecutor[R, El <: HList, In <: HList, CIn <: HList, Fun, FOut] extends EndpointExecutor[R, El, In, CIn, CIn, Fun, FOut] {

  protected def execute(input: CIn, endpoint: Endpoint[El, In, CIn, CIn, Fun, FOut]): FOut = 
    endpoint.apply(input)
}

trait ReqBodyExecutor[R, El <: HList, In <: HList, Bd, ROut <: HList, POut <: HList, CIn <: HList, Fun, FOut] extends EndpointExecutor[R, El, In, (BodyType[Bd], ROut), CIn, Fun, FOut] {

  implicit def prepend: Prepend.Aux[ROut, Bd :: HNil, POut]
  implicit def eqProof: POut =:= CIn

  protected def execute(input: ROut, body: Bd, endpoint: Endpoint[El, In, (BodyType[Bd], ROut), CIn, Fun, FOut]): FOut = 
    endpoint.apply(input :+ body)
}