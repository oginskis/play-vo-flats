package services

import model.b2b.FlatRequestQuery
import model.b2c.Flat

/**
  * Created by oginskis on 21/05/2017.
  */
trait FlatExtractor {
  def extractFlats(flatQuery: FlatRequestQuery): List[Flat]
}
