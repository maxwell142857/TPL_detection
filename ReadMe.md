# 方法说明
### AndroidCFG
负责处理.apk文件并在SootOutput目录下生成该apk的所有方法的CFG。
生成CFG的相关配置在MyAnalysis.java中
### JarCFG
负责将./target目录及子目录中所有的.class文件生成CFG于SootOutput目录下
### ZipUtil
负责将./target目录下的所有压缩包解压并在./target生成对应内容
### Serialization
遍历SootOutput路径，对于的CFG，先调整成我们定义的格式，再哈希
### Compare
对比两个txt文件中的哈希值，计算hit rate

# 使用流程
1 跑AndroidCFG，生成.dot文件  
2 跑 serialization,注意调整输出的文件名，比如把结果存储在database.txt中  
3 删除SootOutput目录  
4 跑JarCFG，生成.dot文件  
5 跑 serialization,注意调整输出的文件名，result.txt中  
6 使用Compare，计算result.txt在database.txt中的 hit rate  
# 当前的发现
1一个第三方库的方法，并不全在.apk中，可能的原因：编译器优化（分析源码，验证是否是这个原因）  
2 现在是允许伪类生成，apk->CFG是完备的，不需要使用伪类；jar/aar->CFG,如果没有伪类，可能报错
配置在JarCFG中的59行soot_args.add("-allow-phantom-refs");现在的主要问题就是jar包的依赖  
3 现在生成CFG的hash没有加入方法名，如果需要加入，将serialization中的第70行代码注释回来即可
a.b.