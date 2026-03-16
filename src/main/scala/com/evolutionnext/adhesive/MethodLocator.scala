package com.evolutionnext.adhesive

import java.lang.reflect.Method

trait MethodLocator {
  def findMethod(step:String):Option[Method]
}
