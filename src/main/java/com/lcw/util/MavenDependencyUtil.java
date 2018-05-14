package com.lcw.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.repository.legacy.repository.ArtifactRepositoryFactory;
import org.apache.maven.repository.legacy.repository.DefaultArtifactRepositoryFactory;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author lancw
 */
public class MavenDependencyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(MavenDependencyUtil.class.getName());
    private static final ArtifactRepositoryFactory FACTORY = new DefaultArtifactRepositoryFactory();
    private static final String MAVEN_HOME = "MAVEN_HOME";
    private static final String M2_HOME = "M2_HOME";
    private static final String MAVEN_HOME_END = "\\bin\\..";
    private static final String SUFFIX_JAR = ".jar";
    private static final String SUFFIX_WAR = ".war";
    private static final String INFO = "[INFO]";
    private static final String BF = "BUILD FAILURE";
    private static String mavenHome;
    private static final HashMap<String, ArrayList<URL>> CACHE = new HashMap<>();

    /**
     * 解析项目pom文件，返回依赖jar的URL数组
     *
     * @param pomPath
     * @return
     * @throws Exception
     */
    public static ArrayList<URL> analysisPOM(String pomPath) throws Exception {
        try {
            if (CACHE.isEmpty()) {
                CACHE.putAll((Map<String, ArrayList<URL>>) SerializableUtil.readObject(SerializableUtil.FileNameEnum.DEPENDENCY_CACHE));
            }
        } catch (Exception e) {
            LOGGER.info("未找到依赖关系缓存,将从maven依赖中分析");
        }
        if (CACHE.get(pomPath) != null) {
            LOGGER.info("get URLs from cache");
            return CACHE.get(pomPath);
        }
        File pf = new File(pomPath);
        ArrayList<URL> urls = new ArrayList<>();
        if (pf.isDirectory()) {
            getSubFile(pf, urls, 1);
            if (!urls.isEmpty()) {
                return urls;
            } else {
                throw new Exception(pomPath + "文件夹中没有任何jar文件");
            }
        }
        ArtifactRepository localRepository = localRepository();
        File pomDir = new File(pomPath).getParentFile();
        String cmd = System.getProperties().getProperty("os.name").toUpperCase().contains("WINDOWS") ? "cmd /c " : "";
        Process p = Runtime.getRuntime().exec(cmd + "mvn dependency:list", null, pomDir);
        boolean error = false;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String tmp = br.readLine();
            while (tmp != null) {
                LOGGER.info(tmp);
                if (tmp.startsWith(INFO) && tmp.contains(":jar:")) {//io.netty:netty:jar:3.7.0.Final:compile
                    tmp = tmp.replace(INFO, "").trim();
                    String[] args = tmp.split(":");
                    Artifact a = new DefaultArtifact(args[0], args[1], args[3], args[4], args[2], null, new DefaultArtifactHandler());
                    File file = new File(localRepository.getBasedir(), localRepository.pathOf(a) + SUFFIX_JAR);
                    if (!file.exists()) {
                        LOGGER.error("file not found:" + file.getAbsolutePath());
                    } else {
                        urls.add(file.toURI().toURL());
                    }
                } else if (tmp.contains(BF)) {
                    error = true;
                }
                tmp = br.readLine();
            }
        }
        if (error) {
            throw new Exception("项目构建失败，请修复项目错误！");
        }
        p.destroy();
        File target = new File(pomDir.getAbsolutePath() + "/target");
        if (!target.exists()) {
            p = Runtime.getRuntime().exec(cmd + "mvn install -Dmaven.test.skip=true", null, pomDir);
            try (BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
                String tmp = br.readLine();
                while (tmp != null) {
                    LOGGER.info(tmp);
                    if (tmp.contains(BF)) {
                        error = true;
                    }
                    tmp = br.readLine();
                }
            }
        }
        if (error) {
            throw new Exception("项目构建失败，请修复项目错误！");
        }
        File[] jar = target.listFiles((File dir, String name) -> name.endsWith(SUFFIX_JAR) || name.endsWith(SUFFIX_WAR));
        if (jar != null) {
            for (File file : jar) {
                urls.add(file.toURI().toURL());
            }
        }
        if (urls.size() > 0) {
            CACHE.put(pomPath, urls);
            SerializableUtil.saveObject(CACHE, SerializableUtil.FileNameEnum.DEPENDENCY_CACHE);
        }
        return urls;
    }

    private static void getSubFile(File f, ArrayList<URL> urls, int deep) throws MalformedURLException {
        for (File file : f.listFiles()) {
            if (file.isDirectory() && deep < 30) {
                getSubFile(file, urls, ++deep);
            } else if (file.getName().endsWith(".jar")) {
                urls.add(file.toURI().toURL());
            }
        }
    }

    /**
     * 从系统环境变量中查找mavenHome
     *
     * @return
     */
    public static String getMavenHome() {
        if (mavenHome == null) {
            mavenHome = System.getenv(M2_HOME);
            if (StringUtils.isBlank(mavenHome)) {
                LOGGER.error("查询" + M2_HOME + "失败,继续查询" + MAVEN_HOME);
                mavenHome = System.getenv(MAVEN_HOME);
            }
            if (mavenHome == null || mavenHome.trim().isEmpty()) {
                LOGGER.error("查询" + MAVEN_HOME + "失败");
            } else {
                mavenHome = mavenHome.endsWith(MAVEN_HOME_END) ? mavenHome.replace(MAVEN_HOME_END, "") : mavenHome;
                LOGGER.info(M2_HOME + ":" + mavenHome);
            }
        }
        mavenHome = mavenHome == null ? mavenHome : mavenHome.replace("\\", "/");
        return mavenHome;
    }

    /**
     * maven仓库处理
     *
     * @return
     * @throws Exception
     */
    private static ArtifactRepository localRepository() throws Exception {
        if (mavenHome == null) {
            getMavenHome();
        }
        SettingsBuildingRequest request = new DefaultSettingsBuildingRequest();
        request.setGlobalSettingsFile(new File(mavenHome, "/conf/settings.xml"));

        DefaultSettingsBuilder builder = new DefaultSettingsBuilderFactory().newInstance();
        SettingsBuildingResult ret = builder.build(request);

        String path = ret.getEffectiveSettings().getLocalRepository();
        path = path == null ? String.format("%s%s", System.getProperty("user.home"), "\\.m2\\repository") : path;
        return FACTORY.createArtifactRepository("local", "file://" + path, (ArtifactRepositoryLayout) new DefaultRepositoryLayout(), null, null);
    }

    public static void clearCache() {
        SerializableUtil.clearCache(SerializableUtil.FileNameEnum.DEPENDENCY_CACHE);
        CACHE.clear();
    }

}
