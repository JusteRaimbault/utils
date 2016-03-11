import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.{ TransformType, DftNormalization, FastFourierTransformer }
import org.apache.commons.math3.util.MathArrays

import scala.math._

object Convolution {

   /**
    * Moran index using fast convolution.
    *
    * @param matrix
    * @return
    */
   /*def moran_convol(matrix: Array[Array[T]],quantity: Quantity[T]): Double = {
     val conf = matrix.map { row => row.map(quantity).toArray }.toArray
     val n = conf.length
     val flatConf = conf.flatten
     val popMean = flatConf.sum / flatConf.length
     val centeredConf = conf.map { r => r.map { d => d - popMean } }
     val variance = MathArrays.ebeMultiply(centeredConf.flatten, centeredConf.flatten).sum
     val weights = spatialWeights(2 * n - 1)
     val totWeight = Convolution.convolution2D(Array.fill(n, n) { 1.0 }, weights).flatten.sum
     flatConf.length / (totWeight * variance) * MathArrays.ebeMultiply(centeredConf.flatten, Convolution.convolution2D(centeredConf, weights).flatten).sum
   }

   def spatialWeights(n: Int): Array[Array[Double]] = {
     Array.tabulate(n, n) { (i, j) => if (i == n / 2 && j == n / 2) 0.0 else 1 / Math.sqrt((i - n / 2) * (i - n / 2) + (j - n / 2) * (j - n / 2)) }
   }
*/

   /**
    * Mean distance using fast convolution.
    *
    * @param matrix
    * @return
    */
   /*def distance_convol(matrix: Matrix[T],quantity: Quantity[T]): Double = {
     val conf = matrix.map { row => row.map(quantity).toArray }.toArray
     val totPop = conf.flatten.sum
     val dmat = distanceMatrix(2 * conf.length - 1)
     val conv = Convolution.convolution2D(conf, dmat)
     math.sqrt(math.Pi) / (conf.length * totPop * totPop) * MathArrays.ebeMultiply(conv.flatten, conf.flatten).sum
   }
*/

   /**
    * Distance kernel
    *
    * @param n
    * @return
    */
/*
   def distanceMatrix(n: Int): Array[Array[Double]] = {
     Array.tabulate(n, n) { (i, j) => Math.sqrt((i - n / 2) * (i - n / 2) + (j - n / 2) * (j - n / 2)) }
   }
*/



  /**
   * Generic convol for double Arrays (in O(nlog(n)), using FFT)
   *
   * @param x
   * @param k centered kernel
   * @return y = x*k with y_i = \sum_{j=1}{|K|}{x_{i-j-|K|/2}*k_j}
   */
  def convolution(x: Array[Double], k: Array[Double]): Array[Double] = {
    val xl = pow(2.0, ceil(log(x.length) / log(2.0)) + 1)
    val xp = x.padTo(x.length + (xl.toInt - x.length) / 2, 0.0).reverse.padTo(xl.toInt, 0.0).reverse
    val kp = k.padTo(k.length + (xl.toInt - k.length) / 2, 0.0).reverse.padTo(xl.toInt, 0.0).reverse
    val tr = new FastFourierTransformer(DftNormalization.STANDARD)
    val ftx = tr.transform(xp, TransformType.FORWARD)
    val ftk = tr.transform(kp, TransformType.FORWARD)
    val real = MathArrays.ebeSubtract(MathArrays.ebeMultiply(ftx.map { z => z.getReal }, ftk.map { z => z.getReal }), MathArrays.ebeMultiply(ftx.map { z => z.getImaginary }, ftk.map { z => z.getImaginary }))
    val im = MathArrays.ebeAdd(MathArrays.ebeMultiply(ftx.map { z => z.getReal }, ftk.map { z => z.getImaginary }), MathArrays.ebeMultiply(ftx.map { z => z.getImaginary }, ftk.map { z => z.getReal }))
    val trinv = tr.transform(Array.tabulate(real.length) { i => new Complex(real(i), im(i)) }, TransformType.INVERSE).map { z => z.getReal }
    trinv.splitAt(trinv.length - x.length / 2)._2 ++ trinv.splitAt(x.length - x.length / 2)._1
  }

  /**
   * Square convol (for tests)
   *
   * @param x
   * @param k
   * @return
   */
  def directConvol(x: Array[Double], k: Array[Double]): Array[Double] = {
    val kl = k.length
    val xpadded = x.padTo(x.length + kl, 0.0).reverse.padTo(x.length + 2 * kl, 0.0).reverse
    Array.tabulate(x.length + k.length) { i => MathArrays.ebeMultiply(k.reverse, xpadded.splitAt(i + 1)._2.splitAt(k.length)._1).sum }
  }

  /**
   *  2D convolution
   *  Using bijection [|1,N|]2 ~ [|1,N|] by flattening, after having good paddling
   *
   * @param x
   * @param k
   */
  def convolution2D(x: Array[Array[Double]], k: Array[Array[Double]]): Array[Array[Double]] = {
    val xpad = x.map { row => row.padTo(2 * (row.length / 2) + 1 + k(0).length, 0.0).reverse.padTo(2 * (row.length / 2) + 1 + 2 * k(0).length, 0.0).reverse }.padTo(2 * (x.length / 2) + 1 + k.length, Array.fill(2 * (x(0).length / 2) + 1 + 2 * k(0).length) { 0.0 }).reverse.padTo(2 * (x.length / 2) + 1 + 2 * k.length, Array.fill(2 * (x(0).length / 2) + 1 + 2 * k(0).length) { 0.0 })
    val xpos = Array.fill(x.length, x(0).length) { 1.0 }.map { row => row.padTo(2 * (row.length / 2) + 1 + k(0).length, 0.0).reverse.padTo(2 * (row.length / 2) + 1 + 2 * k(0).length, 0.0).reverse }.padTo(2 * (x.length / 2) + 1 + k.length, Array.fill(2 * (x(0).length / 2) + 1 + 2 * k(0).length) { 0.0 }).reverse.padTo(2 * (x.length / 2) + 1 + 2 * k.length, Array.fill(2 * (x(0).length / 2) + 1 + 2 * k(0).length) { 0.0 }).flatten
    val kpad = k.map { row => row.padTo(row.length + (xpad(0).length - row.length) / 2, 0.0).reverse.padTo(xpad(0).length, 0.0).reverse }.padTo(k.length + (xpad.length - k.length) / 2, Array.fill(xpad(0).length) { 0.0 }).reverse.padTo(xpad.length, Array.fill(xpad(0).length) { 0.0 })
    val flatconv = convolution(xpad.flatten, kpad.flatten)
    flatconv.zipWithIndex.filter { case (_, j) => xpos(j) > 0 }.map { case (d, _) => d }.sliding(x(0).length, x.length).toArray.reverse
  }

}
