package de.clusteval.utils.plot;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.rosuda.REngine.REXPMismatchException;
import org.rosuda.REngine.REngineException;
import org.rosuda.REngine.Rserve.RserveException;

import utils.ArraysExt;
import utils.SimilarityMatrix;
import de.clusteval.data.DataConfig;
import de.clusteval.data.dataset.AbsoluteDataSet;
import de.clusteval.data.dataset.DataMatrix;
import de.clusteval.data.dataset.DataSet;
import de.clusteval.data.dataset.format.InvalidDataSetFormatVersionException;
import de.clusteval.data.dataset.format.UnknownDataSetFormatException;
import de.clusteval.framework.MyRengine;
import de.clusteval.run.result.ParameterOptimizationResult;

/**
 * @author Christian Wiwie
 * TODO: do we need this class?
 */
public abstract class Plotter {

	/**
	 * @param result
	 */
	public static void plotParameterOptimizationResult(
			final ParameterOptimizationResult result) {
//
//		MyRengine rEngine;
//		try {
//			rEngine = new MyRengine("");
//			try {
//				rEngine.eval("Sys.setlocale(category='LC_NUMERIC',locale='C')");
//				/*
//				 * Define functions
//				 */
//				rEngine.eval("getDensity <- function(x) {"
//						+ "	return (c(strsplit(x=as.character(x[1]),split=',')[[1]][1],x[-1]))}");
//
//				rEngine.eval("plotDensityVSQuality <- function(title, path, densityParam) {"
//						+ "data <- t(apply(read.table(path, sep='\t',header=TRUE), 1, getDensity));"
//						+ "svg(filename=paste(path,'.svg',sep=''));"
//						// + "par(cex=.5);"
//						+ "matplot(x=data[,1],data[,-1],xlab=densityParam,ylab='Clustering quality',main=title,type='p',pch=20,col=1:6);"
//						+ "legend('topleft',legend=colnames(data)[-1],col=1:6,pch=20);"
//						+ "dev.off();"
//						+ "png(filename=paste(path,'.png',sep=''));"
//						// + "par(cex=.5);"
//						+ "matplot(x=data[,1],data[,-1],xlab=densityParam,ylab='Clustering quality',main=title,type='p',pch=20,col=1:6);"
//						+ "legend('topleft',legend=colnames(data)[-1],col=1:6,pch=20);"
//						+ "dev.off()" + "}");
//				rEngine.eval("plotDensityVSQuality("
//						+ "'"
//						+ result.getMethod().getProgramConfig().getProgram()
//								.getMajorName()
//						+ " vs. "
//						+ result.getMethod().getDataConfig().getDatasetConfig()
//								.getDataSet().getFullName() + "'," + "'"
//						+ result.getAbsolutePath() + "', " + "'"
//						+ result.getMethod().getPlotDensityParameter() + "')");
//			} finally {
//				rEngine.close();
//			}
//		} catch (RserveException e) {
//		}

	}

	/**
	 * This method creates a file containing isoMDS coordinates using the
	 * similarity matrix file of the given data configuration.
	 * 
	 * @param dataConfig
	 * @throws UnknownDataSetFormatException
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws REngineException
	 */
	public static void assessAndWriteIsoMDSCoordinates(
			final DataConfig dataConfig) throws UnknownDataSetFormatException,
			InvalidDataSetFormatVersionException, IllegalArgumentException,
			IOException, REngineException {

//		try {
//			MyRengine rEngine = new MyRengine("");
//			try {
//				DataSet absStandard = dataConfig.getDatasetConfig()
//						.getDataSet().getInStandardFormat();
//
//				String newPath = dataConfig.getDatasetConfig().getDataSet()
//						.getOriginalDataSet().getAbsolutePath()
//						+ ".isoMDS";
//				if (new File(newPath).exists())
//					return;
//
//				boolean wasLoaded = absStandard.isInMemory();
//				if (!wasLoaded)
//					absStandard.loadIntoMemory();
//				SimilarityMatrix simMatrix = (SimilarityMatrix) dataConfig
//						.getDatasetConfig().getDataSet().getInStandardFormat()
//						.getDataSetContent();
//				double[][] sims = simMatrix.toArray();
//				String[] ids = new String[simMatrix.getIds().size()];
//				for (Map.Entry<String, Integer> entry : simMatrix.getIds()
//						.entrySet())
//					ids[entry.getValue()] = entry.getKey();
//				if (!wasLoaded)
//					absStandard.unloadFromMemory();
//
//				rEngine.assign("x",
//						ArraysExt.subtract(ArraysExt.max(sims), sims));
//				rEngine.assign("labels", ids);
//				rEngine.eval("rownames(x) <- labels;");
//				rEngine.eval("colnames(x) <- labels;");
//				rEngine.eval("dists <- as.dist(x+0.00000000000001);");
//				rEngine.eval("library(MASS);");
//				rEngine.eval("iso <- isoMDS(dists)$points;");
//				double[][] isoMDS = rEngine.eval("iso").asDoubleMatrix();
//
//				StringBuilder sb = new StringBuilder();
//
//				for (int i = 0; i < ids.length; i++) {
//					sb.append(ids[i]);
//					sb.append("\t");
//					double[] row = isoMDS[i];
//					for (int c = 0; c < row.length; c++) {
//						sb.append(row[c] + "");
//						sb.append("\t");
//					}
//					sb.deleteCharAt(sb.length() - 1);
//					sb.append(System.getProperty("line.separator"));
//				}
//				sb.deleteCharAt(sb.length() - 1);
//
//				BufferedWriter bw = new BufferedWriter(new FileWriter(newPath));
//				bw.append(sb.toString());
//				bw.close();
//			} catch (REXPMismatchException e) {
//				e.printStackTrace();
//			} finally {
//				rEngine.close();
//			}
//		} catch (RserveException e) {
//			e.printStackTrace();
//		}
	}

	/**
	 * @param dataConfig
	 * @throws InvalidDataSetFormatVersionException
	 * @throws IllegalArgumentException
	 * @throws IOException
	 * @throws REngineException
	 */
	public static void assessAndWritePCACoordinates(final DataConfig dataConfig)
			throws InvalidDataSetFormatVersionException,
			IllegalArgumentException, IOException, REngineException {

//		try {
//			MyRengine rEngine = new MyRengine("");
//			try {
//				DataSet standard = dataConfig.getDatasetConfig().getDataSet()
//						.getOriginalDataSet();
//				if (!(standard instanceof AbsoluteDataSet))
//					return;
//
//				AbsoluteDataSet absStandard = (AbsoluteDataSet) standard;
//
//				String newPath = absStandard.getAbsolutePath() + ".PCA";
//				if (new File(newPath).exists())
//					return;
//
//				boolean wasLoaded = absStandard.isInMemory();
//				if (!wasLoaded)
//					absStandard.loadIntoMemory();
//				DataMatrix dataMatrix = absStandard.getDataSetContent();
//				double[][] x = dataMatrix.getData();
//				String[] ids = dataMatrix.getIds();
//				if (!wasLoaded)
//					absStandard.unloadFromMemory();
//
//				rEngine.assign("x", x);
//				rEngine.assign("labels", ids);
//				rEngine.eval("rownames(x) <- labels;");
//				rEngine.eval("pca <- prcomp(x)$x;");
//				double[][] pca = rEngine.eval("pca").asDoubleMatrix();
//
//				StringBuilder sb = new StringBuilder();
//
//				for (int i = 0; i < ids.length; i++) {
//					sb.append(ids[i]);
//					sb.append("\t");
//					double[] row = pca[i];
//					for (int c = 0; c < row.length; c++) {
//						sb.append(row[c] + "");
//						sb.append("\t");
//					}
//					sb.deleteCharAt(sb.length() - 1);
//					sb.append(System.getProperty("line.separator"));
//				}
//				sb.deleteCharAt(sb.length() - 1);
//
//				BufferedWriter bw = new BufferedWriter(new FileWriter(newPath));
//				bw.append(sb.toString());
//				bw.close();
//			} catch (REXPMismatchException e) {
//				e.printStackTrace();
//			} finally {
//				rEngine.close();
//			}
//		} catch (RserveException e) {
//			e.printStackTrace();
//		}
	}
}
