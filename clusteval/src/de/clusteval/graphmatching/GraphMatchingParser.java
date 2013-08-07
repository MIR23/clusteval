/**
 * 
 */
package de.clusteval.graphmatching;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import utils.Pair;
import utils.parse.TextFileParser;
import utils.text.TextFileMapParser;
import de.clusteval.framework.repository.NoRepositoryFoundException;
import de.clusteval.framework.repository.Repository;
import de.clusteval.program.ParameterSet;
import de.clusteval.quality.QualityMeasure;
import de.clusteval.quality.QualityMeasureValue;
import de.clusteval.quality.QualitySet;
import de.clusteval.quality.UnknownQualityMeasureException;

/**
 * A parser for files containing parameter sets and clusterings.
 * 
 * @author Christian Wiwie
 */
public class GraphMatchingParser extends TextFileParser {

	protected Repository repository;

	protected boolean parseQualities;

	/**
	 * This variable holds the results after parsing
	 */
	protected Pair<ParameterSet, GraphMatching> result;

	/**
	 * A temporary variable of no use after parsing.
	 */
	protected List<String> params;

	/**
	 * Instantiates a new clustering parser.
	 * 
	 * @param repository
	 * 
	 * @param absFilePath
	 *            the abs file path
	 * @param parseQualities
	 *            True, if the qualities of the clusterings should also be
	 *            parsed. Those will be taken from .qual-files.
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public GraphMatchingParser(final Repository repository,
			final String absFilePath, final boolean parseQualities)
			throws IOException {
		super(absFilePath, new int[]{0}, new int[]{1});
		this.repository = repository;
		this.setLockTargetFile(true);
		this.params = new ArrayList<String>();
		this.parseQualities = parseQualities;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#processLine(java.lang.String[],
	 * java.lang.String[])
	 */
	@Override
	protected void processLine(String[] key, String[] value) {
		String parameterString = key.length > 0 ? key[0] : "";
		String[] params = parameterString.equals("")
				? new String[0]
				: parameterString.split(",");

		if (this.currentLine == 0) {
			for (String param : params)
				this.params.add(param.intern());
			return;
		}

		GraphMatching result;
		try {

			Repository repo = Repository.getRepositoryForPath(absoluteFilePath);
			if (repo.getParent() != null)
				repo = repo.getParent();

			result = new GraphMatching();

			ParameterSet paramValues = new ParameterSet();
			for (int pos = 0; pos < this.params.size(); pos++) {
				paramValues.put(this.params.get(pos), params[pos]);
			}

			String matchingsString = value[0];
			String[] matchings = matchingsString.split(";");
			for (String matching : matchings) {
				String[] items = matching.split(",");
				result.addMatching(Pair.getPair(items[0], items[1]));
			}

			this.result = Pair.getPair(paramValues, result);
		} catch (NoRepositoryFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see utils.parse.TextFileParser#finishProcess()
	 */
	@Override
	public void finishProcess() {
		// parse qualities
		// TODO
		if (parseQualities) {
			final File qualityFile = new File(this.absoluteFilePath.replace(
					".conv", ".qual"));
			if (qualityFile.exists()) {
				try {
					TextFileMapParser parser = new TextFileMapParser(
							qualityFile.getAbsolutePath(), 0, 1);
					parser.process();
					Map<String, String> result = parser.getResult();
					QualitySet qualitySet = new QualitySet();
					for (String measure : result.keySet()) {
						QualityMeasure clMeasure;
						clMeasure = QualityMeasure.parseFromString(
								this.repository, measure);
						qualitySet.put(clMeasure, QualityMeasureValue
								.getForDouble(Double.parseDouble(result
										.get(measure))));
					}
					this.result.getSecond().setQualities(qualitySet);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (UnknownQualityMeasureException e) {
					e.printStackTrace();
				}
			}
		}

		super.finishProcess();
	}

	/**
	 * @return A map containing parameter sets and corresponding clusterings.
	 */
	public Pair<ParameterSet, GraphMatching> getMatchings() {
		return this.result;
	}
}
