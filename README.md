# ZosShell

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demo.gif)
  
This project demonstrates the usage of [Zowe Java SDK](https://github.com/zowe/zowe-client-java-sdk).
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs the following linux commands:  
  
    cat
    cd
    ls  
    ls -l
    ps
    ps <arg> - where arg is a task/job name   
    pwd   
  
Along with following custom commands:  
    
    count members  
    count datasets  
    visited  
    submit <arg> - where arg is a member name  
    cancel <arg> - where arg is a task/job name  
  
To quit from the command shell UI, you can either press ctrl-c keys or enter 'end' keyword.  
  
## Requirements  
  
    Java 11 to execute and build the target jar file.   
    z/OSMF installed on your backend mainframe instance.  
    
## Build And Execute  

Edit src/main/java/com/ZosShell.java and change the following variables with the needed values to connect to your mainframe instance:  
  
    private static final String hostName = "xxxx";
    private static final String zosmfPort = "xxxx";
    private static final String userName = "xxxx";
    private static final String password = "xxxx";
  
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar  
  

