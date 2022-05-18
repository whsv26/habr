package org.whsv26.habr
package opaque

opaque type CommentCount = Int

def CommentCount: Int => CommentCount = identity

extension (v: CommentCount) {
  def asInt: Int = v
}
