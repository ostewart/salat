/**
 * Copyright (c) 2010, 2011 Novus Partners, Inc. <http://novus.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * For questions and comments about this product, please see the project page at:
 *
 * http://github.com/novus/salat
 *
 */
package com.novus.salat.util

import java.math.BigInteger
import org.bson.types.ObjectId

object `package` {

  val NonePlaceholder = "[None]"
  val NullPlaceholder = "[Null]"
  val EmptyPlaceholder = "[Empty]"

  val SalatThreads = new ThreadGroup("Salat")
  val DefaultSalatStackSize = 1024L * 1024

  def truncate(a: AnyRef, l: Int = 100) = if (a == null) {
    val s = a.toString
    if (s != null && s.length > l) s.substring(0, l) + "..." else s
  }
  else a

  def asyncSalat[T](f: => T): T = asyncSalat[T](DefaultSalatStackSize)(f)

  def asyncSalat[T](stackSize: Long)(f: => T): T = {
    var result: Either[Throwable, T] = Left(new Error("no reply back, boo"))
    def satisfy(r: Either[Throwable, Any]) {
      result = r.asInstanceOf[Either[Throwable, T]]
    }

    val th = new Thread(SalatThreads,
      new AsyncSalatRunnable(f)(satisfy _),
      "Salat-%d".format(System.nanoTime),
      stackSize)

    th.start
    var done = false
    while (!done) {
      try {
        th.join
        done = true
      }
      catch {
        case ie: InterruptedException => {}
      }
    }
    result.right.getOrElse(throw result.left.get)
  }

   // TODO: reflection.  i'm so ashamed.  but not so ashamed i wouldn't do it!
  def reflectFields(x: Any with Product): Map[Any, Any] = {
    val fieldNames: Map[Any, String] = {
      val builder = Map.newBuilder[Any, String]
      for (field: java.lang.reflect.Field <- x.getClass.getDeclaredFields) {
        field.setAccessible(true)
        builder += field.get(x) -> field.getName
      }
      builder.result
    }

    val builder = Map.newBuilder[Any, Any]
    for (v <- x.productIterator) {
      builder += fieldNames(v) -> v
    }
    builder.result
  }

  @deprecated("who's using this?") implicit def shortenOID(oid: ObjectId) = new {
    def asShortString = (new BigInteger(oid.toString, 16)).toString(36)
  }

  @deprecated("who's using this?") implicit def explodeOID(oid: String) = new {
    def asObjectId = new ObjectId((new BigInteger(oid, 36)).toString(16))
  }
}
