/**
 * Copyright (c) 2011-2013, kidzhou 周磊 (zhouleib1412@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cn.dreampie.sqlinxml;

import cn.dreampie.jaxb.JaxbKit;
import com.google.common.collect.Maps;
import com.jfinal.log.Logger;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class SqlKit {

  protected static final Logger LOG = Logger.getLogger(SqlKit.class);

  private static Map<String, String> sqlMap;

  public static String sql(String groupNameAndsqlId) {
    if (sqlMap == null) {
      throw new NullPointerException("SqlInXmlPlugin not start");
    }
    return sqlMap.get(groupNameAndsqlId);
  }

  static void clearSqlMap() {
    sqlMap.clear();
  }

  static void init() {
    sqlMap = new HashMap<String, String>();
    //加载sql文件
    Enumeration<URL> baseURLs = null;
    try {
      baseURLs = SqlKit.class.getClassLoader().getResources("sql");

      if (baseURLs == null) {
        baseURLs = SqlKit.class.getClassLoader().getResources("");
      }
      URL url = null;
      while (baseURLs.hasMoreElements()) {
        url = baseURLs.nextElement();
        loadFilePath(url);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    LOG.debug("sqlMap" + sqlMap);
  }

  static void init(String... paths) {
    sqlMap = new HashMap<String, String>();

    for (String path : paths) {
      if (path.startsWith("/")) {
        path += path.substring(1);
      }
      Enumeration<URL> baseURLs = null;
      try {
        baseURLs = SqlKit.class.getClassLoader().getResources(path);

        if (baseURLs == null) {
          baseURLs = SqlKit.class.getClassLoader().getResources("");
        }
        URL url = null;
        while (baseURLs.hasMoreElements()) {
          url = baseURLs.nextElement();
          loadFilePath(url);
        }

      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    LOG.debug("sqlMap" + sqlMap);
  }

  private static void loadFilePath(URL baseURL) {
    if (baseURL != null) {
      String protocol = baseURL.getProtocol();
      String basePath = baseURL.getFile();
      if ("jar".equals(protocol)) {
        String[] pathurls = basePath.split("!/");
        // 获取jar
        try {
          loadJarFileList(pathurls[0].replace("file:", ""), pathurls[1]);
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        //加载sql文件
        loadFileList(basePath);
      }
    }
  }

  public static void loadFileList(String strPath) {
    List<File> files = new ArrayList<File>();
    File dir = new File(strPath);
    File[] dirs = dir.listFiles(new FileFilter() {
      @Override
      public boolean accept(File pathname) {
        if (pathname.getName().endsWith("sql") || pathname.getName().endsWith("sql.xml")) {
          return true;
        }
        return false;
      }
    });

    if (dirs == null)
      return;
    for (int i = 0; i < dirs.length; i++) {
      if (dirs[i].isDirectory()) {
        loadFileList(dirs[i].getAbsolutePath());
      } else {
        if (dirs[i].getName().endsWith("sql.xml")) {
          files.add(dirs[i]);
        }
      }
    }
    //加载sql文件
    loadFiles(files);
  }


  /**
   * find jar file
   *
   * @param filePath    文件路径
   * @param packageName 包名
   * @return list
   * @throws java.io.IOException 文件读取异常
   */
  private static void loadJarFileList(String filePath, String packageName) throws IOException {
    Map<String, InputStream> sqlFiles = Maps.newHashMap();
    JarFile localJarFile = new JarFile(new File(filePath));
    sqlFiles = findInJar(localJarFile, packageName);
    //加载sql文件
    loadStreamFiles(sqlFiles);
    localJarFile.close();
  }

  /**
   * 从jar里加载文件
   * @param localJarFile 文件路径
   * @param packageName 包名
   * @return
   */
  private static Map<String, InputStream> findInJar(JarFile localJarFile, String packageName) {
    Map<String, InputStream> sqlFiles = Maps.newHashMap();
    Enumeration<JarEntry> entries = localJarFile.entries();
    while (entries.hasMoreElements()) {
      JarEntry jarEntry = entries.nextElement();
      String entryName = jarEntry.getName();
      if (!jarEntry.isDirectory() && (packageName == null || entryName.startsWith(packageName)) && entryName.endsWith("sql.xml")) {
        sqlFiles.put(entryName.substring(entryName.lastIndexOf("/") + 1), SqlKit.class.getResourceAsStream(File.separator + entryName));
      }
    }
    return sqlFiles;
  }

  /**
   * 加载xml文件
   *
   * @param files
   */
  private static void loadFiles(List<File> files) {
    for (File xmlfile : files) {
      SqlRoot root = JaxbKit.unmarshal(xmlfile, SqlRoot.class);
      for (SqlGroup sqlGroup : root.sqlGroups) {

        getSql(xmlfile.getName(), sqlGroup);
      }
    }
  }

  private static void loadStreamFiles(Map<String, InputStream> streams) {
    for (String filename : streams.keySet()) {
      SqlRoot root = JaxbKit.unmarshal(streams.get(filename), SqlRoot.class);
      for (SqlGroup sqlGroup : root.sqlGroups) {

        getSql(filename, sqlGroup);
      }
    }
  }


  private static void getSql(String filename, SqlGroup sqlGroup) {
    String name = sqlGroup.name;
    if (name == null || name.trim().equals("")) {
      name = filename;
    }
    for (SqlItem sqlItem : sqlGroup.sqlItems) {
      sqlMap.put(name + "." + sqlItem.id, sqlItem.value);
    }
  }
}
