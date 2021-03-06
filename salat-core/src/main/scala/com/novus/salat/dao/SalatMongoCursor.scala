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
package com.novus.salat.dao

import com.novus.salat._
import com.mongodb.casbah.Imports._
import com.novus.salat.util.Logging
import com.mongodb.DBCursor
import com.mongodb.casbah.{MongoCursorBase, CursorExplanation, Imports}
import scala.Predef._

/**
 * Unfortunately, MongoCursorBase is typed to DBObject, but....
 * Ripped off from casbah-mapper.
 * https://github.com/maxaf/casbah-mapper/blob/master/src/main/scala/mapper/MappedCollection.scala
 *
 */

trait SalatMongoCursorBase[T <: CaseClass] extends Logging {

  import scalaj.collection.Imports._

  val _grater: Grater[T]

  val underlying: DBCursor

  def next() = _grater.asObject(underlying.next)

  def hasNext = underlying.hasNext

  def sort[A <% DBObject](orderBy: A): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.sort(orderBy)
    this
  }

  def count = underlying.count

  def option_=(option: Int): Unit = underlying.addOption(option)

  def option = underlying.getOptions

  def resetOptions() = underlying.resetOptions() // use parens because this side-effects

  def options = underlying.getOptions

  def options_=(opts: Int): Unit = underlying.setOptions(opts)

  def hint[A <% DBObject](indexKeys: A): this.type = {
    underlying.hint(indexKeys)
    this
  }

  def hint(indexName: String): this.type = {
    underlying.hint(indexName)
    this
  }

  def snapshot(): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.snapshot() // parens for side-effecting
    this
  }

  def explain = new CursorExplanation(underlying.explain)

  def limit(n: Int): this.type = {
    underlying.limit(n)
    this
  }

  def skip(n: Int): this.type = {
    underlying.skip(n)
    this
  }

  def cursorId = underlying.getCursorId()

  def close() = underlying.close() // parens for side-effect

  def slaveOk() = underlying.slaveOk() // parens for side-effect

  def numGetMores = underlying.numGetMores

  def numSeen = underlying.numSeen

  def sizes = underlying.getSizes.asScala

  def batchSize(n: Int) = {
    underlying.batchSize(n)
    this
  }

  def keysWanted = underlying.getKeysWanted

  def query = underlying.getQuery

  def addSpecial(name: String, o: Any): this.type = {
    // The Java code returns a copy of itself (via _this_) so no clone/_newInstance
    underlying.addSpecial(name, o.asInstanceOf[AnyRef])
    this
  }

  def $returnKey(bool: Boolean = true): this.type = addSpecial("$returnKey", bool)

  def $maxScan[A: Numeric](max: T): this.type = addSpecial("$maxScan", max)

  def $query[A <% DBObject](q: A): this.type = addSpecial("$query", q)

  def $orderby[A <% DBObject](obj: A): this.type = addSpecial("$orderby", obj)

  def $explain(bool: Boolean = true): this.type = addSpecial("$explain", bool)

  def $snapshot(bool: Boolean = true): this.type = addSpecial("$snapshot", bool)

  def $min[A <% DBObject](obj: A): this.type = addSpecial("$min", obj)

  def $max[A <% DBObject](obj: A): this.type = addSpecial("$max", obj)

  def $showDiskLoc(bool: Boolean = true): this.type = addSpecial("$showDiskLoc", bool)

  def $hint[A <% DBObject](obj: A): this.type = addSpecial("$hint", obj)

  def _newInstance(cursor: DBCursor): SalatMongoCursorBase[T]

  def copy(): SalatMongoCursorBase[T] = _newInstance(underlying.copy()) // parens for side-effects
}

case class SalatMongoCursor[T <: CaseClass : Manifest](_grater: Grater[T], underlying: DBCursor) extends SalatMongoCursorBase[T] with Iterator[T] {

  def _newInstance(cursor: DBCursor) = SalatMongoCursor(_grater, cursor)
}