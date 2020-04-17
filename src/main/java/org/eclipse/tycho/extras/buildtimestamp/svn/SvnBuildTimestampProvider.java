/*******************************************************************************
 * Copyright (c) 2012 Sonatype Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Sonatype Inc. - initial API and implementation
 *    Zune01 - porting usage of git to svn
 *    fx-world Softwareentwicklung - standalone build and bugfixing
 *******************************************************************************/
package org.eclipse.tycho.extras.buildtimestamp.svn;

import java.io.File;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.MojoExecution;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.tycho.buildversion.BuildTimestampProvider;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.ISVNInfoHandler;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNInfo;
import org.tmatesoft.svn.core.wc.SVNWCClient;

/**
 * Build timestamp provider that returns date of the most recent commit that
 * touches any file under project basedir. File additional flexibility, some
 * files can be ignored using file list specified in &lt;svn.ignore&gt;
 * element of tycho-packaging-plugin configuration block
 * 
 * <p>
 * Typical usage
 * 
 * <pre>
 * ...
 *       &lt;plugin&gt;
 *         &lt;groupId&gt;org.eclipse.tycho&lt;/groupId&gt;
 *         &lt;artifactId&gt;tycho-packaging-plugin&lt;/artifactId&gt;
 *         &lt;version&gt;${tycho-version}&lt;/version&gt;
 *         &lt;dependencies&gt;
 *           &lt;dependency&gt;
 *             &lt;groupId&gt;de.fx-world&lt;/groupId&gt;
 *             &lt;artifactId&gt;tycho-buildtimestamp-svn&lt;/artifactId&gt;
 *             &lt;version&gt;0.19.0&lt;/version&gt;
 *           &lt;/dependency&gt;
 *         &lt;/dependencies&gt;
 *         &lt;configuration&gt;
 *           &lt;timestampProvider&gt;svn&lt;/timestampProvider&gt;
 *           &lt;svn.ignore&gt;pom.xml&lt;/svn.ignore&gt;
 *         &lt;/configuration&gt;
 *       &lt;/plugin&gt;
 * ...
 * </pre>
 */
@Component(role = BuildTimestampProvider.class, hint = "svn")
public class SvnBuildTimestampProvider implements BuildTimestampProvider {

	public Date getTimestamp(MavenSession session, MavenProject project, MojoExecution execution) throws MojoExecutionException {
		SVNClientManager  clientManager = SVNClientManager.newInstance();
		SVNWCClient       wcClient      = clientManager.getWCClient();		
		String            ignoreFilter  = getIgnoreFilter(execution);
		final Date[]      result        = { null };
		final Set<String> filterFiles   = new HashSet<String>();

		if (ignoreFilter != null) {
			StringTokenizer tokens = new StringTokenizer(ignoreFilter, "\n\r\f");
			while (tokens.hasMoreTokens()) {
				filterFiles.add(tokens.nextToken());
			}
		}
		
		try {
			wcClient.doInfo(project.getBasedir(), null, null, SVNDepth.INFINITY, null, new ISVNInfoHandler() {
				public void handleInfo(SVNInfo info) throws SVNException {
					File file = info.getFile();
					if (filterFiles.contains(file.getName())) {
						return;
					}
					Date date = info.getCommittedDate();
					if (result[0] == null || date.after(result[0])) {
						result[0] = date;
					}
				}
			});
		} catch (SVNException e) {
			throw new MojoExecutionException("Failed to get info", e);
		}

		return result[0];
	}

	public String getTimestampString(MavenSession session, MavenProject project, MojoExecution execution) throws MojoExecutionException {
		SVNClientManager  clientManager = SVNClientManager.newInstance();
		SVNWCClient       wcClient      = clientManager.getWCClient();		
		String            ignoreFilter  = getIgnoreFilter(execution);
		final String[]    result        = { "" };
		final Set<String> filterFiles   = new HashSet<String>();

		if (ignoreFilter != null) {
			StringTokenizer tokens = new StringTokenizer(ignoreFilter, "\n\r\f");
			while (tokens.hasMoreTokens()) {
				filterFiles.add(tokens.nextToken());
			}
		}
		
		try {
			wcClient.doInfo(project.getBasedir(), null, null, SVNDepth.EMPTY, null, new ISVNInfoHandler() {
				public void handleInfo(SVNInfo info) throws SVNException {
					long committedRevision = info.getCommittedRevision().getNumber();
					result[0] = Long.toString(committedRevision);
				}
			});
		} catch (SVNException e) {
			throw new MojoExecutionException("Failed to get info", e);
		}

		return result[0];
	}

	private String getIgnoreFilter(MojoExecution execution) {
		String result = null;
		Xpp3Dom pluginConfiguration = (Xpp3Dom) execution.getPlugin().getConfiguration();
		Xpp3Dom ignoreDom = pluginConfiguration.getChild("svn.ignore");

		if (ignoreDom != null) {
			result = ignoreDom.getValue();
		}

		return result;
	}
}
