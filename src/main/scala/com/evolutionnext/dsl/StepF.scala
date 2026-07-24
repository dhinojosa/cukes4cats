package com.evolutionnext.dsl

import cats.Applicative
import cats.data.StateT

type StepF[F[_], Context] = StateT[F, Context, Unit]

object StepF:

  def modify[F[_]: Applicative, C](f: C => C): StepF[F, C] =
    StateT.modify(f)

  def inspect[F[_]: Applicative, C](f: C => Unit): StepF[F, C] =
    StateT.inspect(f)

  def liftF[F[_]: Applicative, C](fa: F[Unit]): StepF[F, C] =
    StateT.liftF(fa)
