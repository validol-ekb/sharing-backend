package ekb.validol.sharing.backend.model

case class Sharing(users: Seq[String], selections: Map[SheetName.Value, Seq[Selection]])