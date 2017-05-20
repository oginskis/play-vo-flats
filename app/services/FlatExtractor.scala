package services

import model.Flat

/**
  * Created by oginskis on 21/05/2017.
  */
trait FlatExtractor {
  def extractFlats() : List[Flat]
}
