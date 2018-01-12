/**
 * Copyright © 2017 Jesse Gallagher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Includes code derived from the JNoSQL Diana Couchbase driver and Artemis
 * extensions, copyright Otavio Santana and others and available from:
 *
 * https://github.com/eclipse/jnosql-diana-driver/tree/master/couchbase-driver
 * https://github.com/eclipse/jnosql-artemis-extension/tree/master/couchbase-extension
 */
package org.openntf.domino.jnosql.diana.document;

import static com.ibm.commons.util.StringUtil.format;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.openntf.domino.AutoMime;
import org.openntf.domino.Database;
import org.openntf.domino.Session;
import org.openntf.domino.Database.FTIndexOption;
import org.openntf.domino.session.ISessionFactory;
import org.openntf.domino.utils.Factory;
import org.openntf.domino.utils.Factory.SessionType;
import org.openntf.domino.utils.Factory.ThreadConfig;

import com.ibm.commons.util.io.StreamUtil;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;

public abstract class AbstractDominoAppTest {
	
	public static String BLANK_DB;
	static lotus.domino.Session lotusSession;
	private static final List<String> TO_DELETE_DBS = new ArrayList<>();

	@BeforeClass
	public static void createEnvironment() throws Exception {
		try {
			NotesThread.sinitThread();
			Factory.startup();
			Factory.initThread(new ThreadConfig(new Session.Fixes[0], AutoMime.WRAP_NONE, true));
			
			Factory.setSessionFactory(new ISessionFactory() {
				private static final long serialVersionUID = 1L;

				@Override
				public Session createSession() {
					try {
						lotusSession = NotesFactory.createSession();
						return Factory.getWrapperFactory().fromLotus(lotusSession, Session.SCHEMA, null);
					} catch(NotesException ne) {
						throw new RuntimeException(ne);
					}
				}
				
			}, SessionType.CURRENT);
			
			BLANK_DB = instantiateDb("blank");
			
			Factory.getSession().getDatabase(BLANK_DB).createFTIndex(EnumSet.noneOf(FTIndexOption.class), false);
		} catch (Throwable t) {
			t.printStackTrace();
			throw t;
		}
	}
	
	@AfterClass
	public static void teardown() throws Exception {
		Factory.termThread();
		Factory.shutdown();
		lotusSession.recycle();
		NotesThread.stermThread();

		for(String path : TO_DELETE_DBS) {
			deleteDb(path);
		}
	}
	
	/* ******************************************************************************
	 * Internal utility methods
	 ********************************************************************************/

	private static String instantiateDb(final String basename) throws IOException {
		File dbFile = File.createTempFile(basename, ".nsf"); //$NON-NLS-1$
		System.out.println(format("{0} location is {1}", basename, dbFile.getAbsolutePath())); //$NON-NLS-1$
		FileOutputStream fos = new FileOutputStream(dbFile);
		InputStream is = AbstractDominoAppTest.class.getResourceAsStream("/" + basename + ".nsf"); //$NON-NLS-1$ //$NON-NLS-2$
		StreamUtil.copyStream(is, fos);
		fos.flush();
		StreamUtil.close(fos);
		StreamUtil.close(is);
		TO_DELETE_DBS.add(dbFile.getAbsolutePath());
		return dbFile.getAbsolutePath();
	}
	private static void deleteDb(final String dbName) {
		try {
			File testDB = new File(dbName);
			if(testDB.exists()) {
				testDB.delete();
			}
		} catch(Exception e) { }
	}
}
