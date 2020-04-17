# Tycho Buildtimestamp SVN

Build timestamp provider that returns date of the most recent commit that
touches any file under project basedir. File additional flexibility, some
files can be ignored using file list specified in <svn.ignore>
element of tycho-packaging-plugin configuration block
 
## Typical usage

	<plugin>
		<groupId>org.eclipse.tycho</groupId>
		<artifactId>tycho-packaging-plugin</artifactId>
		<version>${tycho-version}</version>
		<dependencies>
			<dependency>
				<groupId>de.fx-world</groupId>
				<artifactId>tycho-buildtimestamp-svn</artifactId>
				<version>1.7.0-SNAPSHOT</version>
			</dependency>
		</dependencies>
		<configuration>
			<timestampProvider>svn</timestampProvider>
			<svn.ignore>pom.xml</svn.ignore>
		</configuration>
	</plugin>
 	
## License
 
All rights reserved. This program and the accompanying materials
are made available under the terms of the Eclipse Public License v1.0
which accompanies this distribution, and is available at

 [http://www.eclipse.org/legal/epl-v10.html](http://www.eclipse.org/legal/epl-v10.html)
