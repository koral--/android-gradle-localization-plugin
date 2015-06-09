package pl.droidsonroids.gradle.localization
/**
 * Created by BSDC-ZLS on 2015/5/28.
 */
public class XmlUpdate {


    public void update(ConfigExtension config) {
//        if (config.outputDirectory != null) {
//            def dir = config.outputDirectory.deleteDir()
//            println("outRes delete dir:" + dir)
//            if (!config.outputDirectory.exists()) {
//                config.outputDirectory.mkdirs()
//            }
//        }
//        if (config.report != null) {
//            def dir = config.report.deleteDir()
//            println("report delete dir:" + dir)
//            if (!config.report.exists()) {
//                config.report.mkdirs()
//            }
//        }
        update(config.outputDirectory, config.inputDirectory, config.map,
                config.append, config.replace, config.report)
    }

    public void update(File outFile, File inFile, Map<String, String> map,
                       boolean append, boolean replace, File report) {
        def outStrs = outFile.list()

        for (int i = 0; i < outStrs.length; i++) {
            def iFile = new File(inFile, getNewKey(outStrs[i], map) + "/strings.xml")
            def oFile = new File(outFile, outStrs[i] + "/strings.xml")

            //source string res not exists
            if (!iFile.exists()) {
                if (iFile.getParentFile().mkdir()) {
                    iFile.createNewFile()
                    iFile.write(oFile.getText("utf-8"), "utf-8")
                    def xml = parseFromXml(iFile)
                    def parentName = iFile.getParentFile().getName()
                    println((i + 1) + "." + parentName + " " + "Changed:0" + " " + "Append:" + xml.size())
                }
            } else {
                def reportFile = (report == null) ? null : new File(report, outStrs[i] + ".txt")
                reWrite(i, iFile, oFile, append, replace, reportFile);
            }
        }
    }

    public String getNewKey(String key, Map<String, String> map) {
        String ss;
        if (map == null) {
            ss = key;
        } else {
            if (map.get(key) == null) {
                ss = key;
            } else {
                ss = map.get(key);
            }
        }
        return ss;
    }

    // value = value.replace("n", "\\n")
    public void reWrite(int index, File iFile, File oFile,
                        boolean append, boolean replace, File report) {
        String parentDirName = iFile.getParentFile().getName();

        String log = parentDirName;
        if (report == null) {
            if (replace) {
                //Update of TAG
                Set<String> intersect = replaceFile(iFile, oFile);
                log = "Changed:" + intersect.size() + " ";
            }

            if (append) {
                //Lack of TAG
                def minus = appendFile(iFile, oFile);
                log += "Append:" + minus.size()
            }

        } else {
            def iMap = parseFromXml(iFile)
            def oMap = parseFromXml(oFile)

            FileWriter fw = new FileWriter(report)
            if (replace) {
                //Update of TAG
                Set<String> intersect = replaceFile(iFile, oFile);
                log = "Changed:" + intersect.size() + " "

                for (int i = 0; i < intersect.size(); i++) {
                    StringBuilder sb = new StringBuilder()
                    String key = intersect[i];
                    sb.append("Changed." + (i + 1) + "\n")
                    sb.append(key + "\n")
                    sb.append(iMap.get(key) + "\n")
                    sb.append(oMap.get(key) + "\n\n")
                    fw.write(sb.toString())
                }
                fw.append("=========================================================================\n\n")

            }
            if (append) {
                //Lack of TAG
                def minus = appendFile(iFile, oFile);
                log += "Append:" + minus.size()

                for (int i = 0; i < minus.size(); i++) {
                    StringBuilder sb = new StringBuilder()
                    sb.append("Append:" + (i + 1) + "\n")
                    sb.append(minus[i] + "\n");
                    sb.append(oMap.get(minus[i]) + "\n")
                    fw.write(sb.toString() + "\n")
                }
            }
            fw.close()
        }
        if (!parentDirName.equals(log)) {
            println((index + 1) + "." + parentDirName + " " + log)
        }
    }

    public Set<String> replaceFile(File iFile, File oFile) {
        Set<String> sets = new HashSet<String>()

        def iMap = parseFromXml(iFile)
        def oMap = parseFromXml(oFile)

        def intersect = iMap.keySet().intersect(oMap.keySet())

        String contents = iFile.getText('UTF-8')
        String newContents = contents;
        for (String key : intersect) {
            if (!iMap.get(key).equals(oMap.get(key))) {
                sets.add(key)
                newContents = replaceTag(newContents, key, iMap.get(key), oMap.get(key))
            }
        }
        iFile.write(newContents, 'UTF-8')
        return sets
    }

    public Set<String> appendFile(File iFile, File oFile) {
        def iMap = parseFromXml(iFile)
        def oMap = parseFromXml(oFile)

        def minus = oMap.keySet().minus(iMap.keySet())

        String contents = iFile.getText('UTF-8')
        String newContents = contents;
        for (String key : minus) {
            newContents = appendTag(newContents, key, oMap.get(key))
        }
        iFile.write(newContents, 'UTF-8')
        return minus
    }

    /**
     * Reg£ºAny Character£¬Not greed model
     * @param source
     * @param key
     * @param oldValue
     * @param newValue
     * @return
     */
    public static String replaceTag(String source, String key, String oldValue, String newValue) {
        //println("replace " + "key:" + key + ",oldValue:" + oldValue + ",newValue:" + newValue)
        newValue = newValue.replace("\'", "\\\'")
        newValue = newValue.replace("\"", "\\\"");
        def all = source.replaceAll("<string name=\"" + key + "\">" + "[\\s\\S]*?" + "</string>",
                "<string name=\"" + key + "\">" + newValue + "</string>");
        return all;
    }

    public static String appendTag(String source, String key, String value) {
        def all = source.replace("</resources>", "    <string name=\"" + key + "\">"
                + value + "</string>" + "\n" + "</resources>");
        return all;
    }

    public Map<String, String> parseFromXml(File f) {
        Map<String, String> map = new HashMap<>();
        new XmlParser().parse(f).each {
            def name = it.attributes().get('name')
            def value = it.value().text();
            if (!map.containsKey(name)) {
                map.put(name, value)
            }
        }
        return map;
    }
}
