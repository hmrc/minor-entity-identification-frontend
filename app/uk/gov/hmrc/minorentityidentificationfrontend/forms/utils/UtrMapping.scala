/*
 * Copyright 2021 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.minorentityidentificationfrontend.forms.utils

import play.api.data.FormError
import play.api.data.format.Formatter
import uk.gov.hmrc.minorentityidentificationfrontend.models.{Ctutr, Sautr, Utr}

object UtrMapping {

  private val SautrMinLimit = 30000
  private val SuffixLength = 5

  private def getUtrType(utr: String): Utr = {
    val suffix = utr.takeRight(SuffixLength).mkString.toInt

    if (suffix >= SautrMinLimit) Sautr(utr) else Ctutr(utr)
  }

  def utrMapping(errorMessage: String): Formatter[Utr] = new Formatter[Utr] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Utr] =
      data.get(key) match {
        case Some(utr) if utr.nonEmpty =>
          Right(getUtrType(utr))
        case _ =>
          Left(Seq(FormError(key, errorMessage)))
      }

    override def unbind(key: String, value: Utr): Map[String, String] =
      Map(key -> value.value)

  }

}
