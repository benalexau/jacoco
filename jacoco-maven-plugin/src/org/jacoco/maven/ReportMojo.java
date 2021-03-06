/*******************************************************************************
 * Copyright (c) 2009, 2011 Mountainminds GmbH & Co. KG and Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Evgeny Mandrikov - initial API and implementation
 *
 *******************************************************************************/
package org.jacoco.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.maven.doxia.siterenderer.Renderer;
import org.apache.maven.project.MavenProject;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.ICoverageNode;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.report.FileMultiReportOutput;
import org.jacoco.report.IReportGroupVisitor;
import org.jacoco.report.IReportVisitor;
import org.jacoco.report.ISourceFileLocator;
import org.jacoco.report.MultiReportVisitor;
import org.jacoco.report.csv.CSVFormatter;
import org.jacoco.report.html.HTMLFormatter;
import org.jacoco.report.xml.XMLFormatter;

/**
 * Creates a code coverage report for a single project in multiple formats
 * (HTML, XML, and CSV).
 * 
 * @goal report
 * @requiresProject true
 */
public class ReportMojo extends AbstractMavenReport {

	/**
	 * Maven project.
	 * 
	 * @parameter expression="${project}"
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * A list of class files to include in instrumentation/analysis/reports. May
	 * use wildcard characters (* and ?). When not specified - everything will
	 * be included.
	 * 
	 * @parameter expression="${jacoco.includes}"
	 */
	private List<String> includes;

	/**
	 * A list of class files to exclude from instrumentation/analysis/reports.
	 * May use wildcard characters (* and ?).
	 * 
	 * @parameter expression="${jacoco.excludes}"
	 */
	private List<String> excludes;

	/**
	 * Flag used to suppress execution.
	 * 
	 * @parameter expression="${jacoco.skip}" default-value="false"
	 */
	private boolean skip;

    /**
     * <i>Maven Internal</i>: The Doxia Site Renderer.
     * 
     * @component
     */
    private Renderer siteRenderer;
    
    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#canGenerateReport()
     */
    public boolean canGenerateReport() {
		if ("pom".equals(project.getPackaging())) {
			getLog().info(
					"Skipping JaCoCo for project with packaging type 'pom'");
			return false;
		}
		if (skip) {
			getLog().info("Skipping JaCoCo execution");
			return false;
		}
		return true;
	}

    /**
     * @see org.apache.maven.reporting.AbstractMavenReport#isExternalReport()
     */
    public boolean isExternalReport()
    {
        return true;
    }
    
	/**
	 * @return Maven project
	 */
	protected final MavenProject getProject() {
		return project;
	}

	protected List<String> getIncludes() {
		return includes;
	}

	protected List<String> getExcludes() {
		return excludes;
	}

	/**
	 * Output directory for the reports.
	 * 
	 * @parameter default-value="${project.reporting.outputDirectory}/jacoco"
	 */
	private File outputDirectory;

	/**
	 * Encoding of the generated reports.
	 * 
	 * @parameter expression="${project.reporting.outputEncoding}"
	 *            default-value="UTF-8"
	 */
	private String outputEncoding;

	/**
	 * Encoding of the source files.
	 * 
	 * @parameter expression="${project.build.sourceEncoding}"
	 *            default-value="UTF-8"
	 */
	private String sourceEncoding;

	/**
	 * File with execution data.
	 * 
	 * @parameter default-value="${project.build.directory}/jacoco.exec"
	 */
	private File dataFile;

	private SessionInfoStore sessionInfoStore;

	private ExecutionDataStore executionDataStore;

	protected void executeReport(Locale locale) throws MavenReportException {
		try {
			loadExecutionData();
		} catch (final IOException e) {
			getLog().error(
					"Unable to read execution data file " + dataFile + ": "
							+ e.getMessage(), e);
			return;
		}
		try {
			final IReportVisitor visitor = createVisitor();
			visitor.visitInfo(sessionInfoStore.getInfos(),
					executionDataStore.getContents());
			createReport(visitor);
			visitor.visitEnd();
		} catch (final Exception e) {
			getLog().error("Error while creating report: " + e.getMessage(), e);
		}
	}

	@Override
	public String getOutputName() {
		return "jacoco/index";
	}

	@Override
	public String getName(Locale locale) {
		return "JaCoCo";
	}

	@Override
	public String getDescription(Locale locale) {
		return "JaCoCo Test Coverage Report.";
	}

	@Override
	protected Renderer getSiteRenderer() {
		return siteRenderer;
	}

	@Override
	protected String getOutputDirectory() {
		return outputDirectory.getAbsolutePath();
	}

	private void loadExecutionData() throws IOException {
		sessionInfoStore = new SessionInfoStore();
		executionDataStore = new ExecutionDataStore();
		FileInputStream in = null;
		try {
			in = new FileInputStream(dataFile);
			final ExecutionDataReader reader = new ExecutionDataReader(in);
			reader.setSessionInfoVisitor(sessionInfoStore);
			reader.setExecutionDataVisitor(executionDataStore);
			reader.read();
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	private void createReport(final IReportGroupVisitor visitor)
			throws IOException {
		final IBundleCoverage bundle = createBundle();
		final SourceFileCollection locator = new SourceFileCollection(
				getCompileSourceRoots(), sourceEncoding);
		checkForMissingDebugInformation(bundle);
		visitor.visitBundle(bundle, locator);
	}

	private void checkForMissingDebugInformation(final ICoverageNode node) {
		if (node.getClassCounter().getTotalCount() > 0
				&& node.getLineCounter().getTotalCount() == 0) {
			getLog().warn(
					"To enable source code annotation class files have to be compiled with debug information.");
		}
	}

	private IBundleCoverage createBundle() throws IOException {
		final CoverageBuilder builder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, builder);
		final File classesDir = new File(getProject().getBuild()
				.getOutputDirectory());

		List<File> filesToAnalyze = getFilesToAnalyze(classesDir);

		for (File file : filesToAnalyze) {
			analyzer.analyzeAll(file);
		}

		return builder.getBundle(getProject().getName());
	}

	private IReportVisitor createVisitor() throws IOException {
		final List<IReportVisitor> visitors = new ArrayList<IReportVisitor>();
		
		File effectiveOutputDirectory = getReportOutputDirectory();
		if (!effectiveOutputDirectory.getName().equals("jacoco")) {
			effectiveOutputDirectory = new File(effectiveOutputDirectory, "jacoco");
		}
		
		getLog().debug("Effective output directory: " + effectiveOutputDirectory.getCanonicalPath());
		effectiveOutputDirectory.mkdirs();

		final XMLFormatter xmlFormatter = new XMLFormatter();
		xmlFormatter.setOutputEncoding(outputEncoding);
		visitors.add(xmlFormatter.createVisitor(new FileOutputStream(new File(
				effectiveOutputDirectory, "jacoco.xml"))));

		final CSVFormatter formatter = new CSVFormatter();
		formatter.setOutputEncoding(outputEncoding);
		visitors.add(formatter.createVisitor(new FileOutputStream(new File(
				effectiveOutputDirectory, "jacoco.csv"))));

		final HTMLFormatter htmlFormatter = new HTMLFormatter();
		// formatter.setFooterText(footer);
		htmlFormatter.setOutputEncoding(outputEncoding);
		// formatter.setLocale(locale);
		visitors.add(htmlFormatter.createVisitor(new FileMultiReportOutput(
				effectiveOutputDirectory)));

		return new MultiReportVisitor(visitors);
	}

	private static class SourceFileCollection implements ISourceFileLocator {

		private final List<File> sourceRoots;
		private final String encoding;

		public SourceFileCollection(final List<File> sourceRoots,
				final String encoding) {
			this.sourceRoots = sourceRoots;
			this.encoding = encoding;
		}

		public Reader getSourceFile(final String packageName,
				final String fileName) throws IOException {
			final String r;
			if (packageName.length() > 0) {
				r = packageName + '/' + fileName;
			} else {
				r = fileName;
			}
			for (final File sourceRoot : sourceRoots) {
				final File file = new File(sourceRoot, r);
				if (file.exists() && file.isFile()) {
					return new InputStreamReader(new FileInputStream(file),
							encoding);
				}
			}
			return null;
		}

		public int getTabWidth() {
			return 4;
		}
	}

	private File resolvePath(final String path) {
		File file = new File(path);
		if (!file.isAbsolute()) {
			file = new File(getProject().getBasedir(), path);
		}
		return file;
	}

	private List<File> getCompileSourceRoots() {
		final List<File> result = new ArrayList<File>();
		for (final Object path : getProject().getCompileSourceRoots()) {
			result.add(resolvePath((String) path));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	protected List<File> getFilesToAnalyze(File rootDir) throws IOException {
		final String includes;
		if (getIncludes() != null && !getIncludes().isEmpty()) {
			includes = StringUtils.join(getIncludes().iterator(), ",");
		} else {
			includes = "**";
		}
		final String excludes;
		if (getExcludes() != null && !getExcludes().isEmpty()) {
			excludes = StringUtils.join(getExcludes().iterator(), ",");
		} else {
			excludes = "";
		}
		return FileUtils.getFiles(rootDir, includes, excludes);
	}

}
