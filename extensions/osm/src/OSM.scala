
import org.nlogo.api._
import org.nlogo.core.Syntax
import org.nlogo.core.Syntax.{ NumberType, ListType }
import org.nlogo.core.LogoList
import org.nlogo.api.ScalaConversions._

import scala.math._


class OSM extends DefaultClassManager {
  def load(manager: PrimitiveManager) {
    manager.addPrimitive("get-patch-var", new GetPatchVar)
    //manager.addPrimitive("slope", new Slope)
  }
}



class GetPatchVar extends Reporter {
  //override def getSyntax = Syntax.reporterSyntax(right = Array(NumberType), ret = ListType)
  override def getSyntax = Syntax.reporterSyntax(right = List(NumberType),ret = ListType)

  def report(args: Array[Argument], context: Context): AnyRef = {
    val n = args(0).getIntValue + 5 // add constant value to skip built-in patch variables
    def world = context.getAgent.world;
    //def patchVar = (_reference)((org.nlogo.nvm.Argument) args(0)).getReporter();
    //def patchVar = ((ReferenceType) args(0)).getReporter
    //Dimension gridSize = new Dimension(world.worldWidth(), world.worldHeight());

    var res = Seq.empty[LogoList]

    for (px <- world.minPxcor to world.maxPxcor) {
      var currentrow = Seq.empty[Double]
      for (py <- world.minPycor to world.maxPycor) {
          def p = world.fastGetPatchAt(px, py);
            //println(p.getVariable(patchVar.reference.vn()));
            currentrow = currentrow :+ p.getVariable(n).asInstanceOf[Double]
          }
          res = res :+ currentrow.toLogoList
      }

      return(res.toLogoList)

  }
}


/*
// reporter template from morphology extension
class Slope extends Reporter {
  override def getSyntax = Syntax.reporterSyntax(right = List(NumberType), ret = ListType)
  def report(args: Array[Argument], context: Context): AnyRef = {
     def patchVar =  Tools.getPatchVar(args,context)
     val res = Measures.slope(patchVar)
     return(List(res._1,res._2).toLogoList)
  }
}
*/


object Tools {


   //get given patch var as Seq[Seq[Double]]
   def getPatchVar(args: Array[Argument], context: Context): Seq[Seq[Double]] = {
     val n = args(0).getIntValue + 5 // add constant value to skip built-in patch variables
     def world = context.getAgent.world;
     //def patchVar = (_reference)((org.nlogo.nvm.Argument) args(0)).getReporter();
     //def patchVar = ((ReferenceType) args(0)).getReporter
     //Dimension gridSize = new Dimension(world.worldWidth(), world.worldHeight());

     var res = Seq.empty[Seq[Double]]

     for (px <- world.minPxcor to world.maxPxcor) {
       var currentrow = Seq.empty[Double]
       for (py <- world.minPycor to world.maxPycor) {
           def p = world.fastGetPatchAt(px, py);
             //println(p.getVariable(patchVar.reference.vn()));
             currentrow = currentrow :+ p.getVariable(n).asInstanceOf[Double]
           }
           res = res :+ currentrow
       }

       return(res)
   }

}







/*
class SampleScalaExtension extends api.DefaultClassManager {
  def load(manager: api.PrimitiveManager) {
    manager.addPrimitive("first-n-integers", IntegerList)
    manager.addPrimitive("my-list", MyList)
    manager.addPrimitive("create-red-turtles", CreateRedTurtles)
  }
}

object IntegerList extends api.DefaultReporter {
  override def getSyntax =
    reporterSyntax(Array(NumberType), ListType)
  def report(args: Array[api.Argument], context: api.Context): AnyRef = {
    val n = try args(0).getIntValue
    catch {
      case e: api.LogoException =>
        throw new api.ExtensionException(e.getMessage)
    }
    if (n < 0)
      throw new api.ExtensionException("input must be positive")
    (0 until n).toLogoList
  }
}

object MyList extends api.DefaultReporter {
  override def getSyntax =
    reporterSyntax(Array(WildcardType | RepeatableType), ListType, 2)
  def report(args: Array[api.Argument], context: api.Context) =
    args.map(_.get).toLogoList
}

object CreateRedTurtles extends api.DefaultCommand with nvm.CustomAssembled {
  override def getSyntax =
    commandSyntax(Array(NumberType, CommandBlockType | OptionalType))
  // the command itself is observer-only. inside the block is turtle code.
  override def getAgentClassString = "O:-T--"
  // only box this once
  private val red = Double.box(15)
  def perform(args: Array[api.Argument], context: api.Context) {
    // the api package doesn't have what we need, so we'll often
    // be dropping down to the agent and nvm packages
    val n = args(0).getIntValue
    val world = context.getAgent.world.asInstanceOf[agent.World]
    val eContext = context.asInstanceOf[nvm.ExtensionContext]
    val nvmContext = eContext.nvmContext
    val builder =
      new agent.AgentSetBuilder(api.AgentKind.Turtle, n)
    for(_ <- 0 until n) {
      val turtle = world.createTurtle(world.turtles)
      turtle.colorDoubleUnchecked(red)
      builder.add(turtle)
      eContext.workspace.joinForeverButtons(turtle)
    }
    // if the optional command block wasn't supplied, then there's not
    // really any point in calling this, but it won't bomb, either
    nvmContext.runExclusiveJob(builder.build(), nvmContext.ip + 1)
    // prim._extern will take care of leaving nvm.Context ip in the right place
  }
  def assemble(a: nvm.AssemblerAssistant) {
    a.block()
    a.done()
  }
}

*/
