# ZosShell

![Demo](https://github.com/frankgiordano/ZosShell/blob/master/demo.gif)
  
This project demonstrates the usage of [Zowe Java SDK](https://github.com/zowe/zowe-client-java-sdk).
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs the following linux commands:  
  
    cat
    cd
    ls <arg> - where arg is a dataset value or empty 
    ls -l
    ps
    ps <arg> - where arg is a task/job name   
    pwd   
    uname
    whoami
  
Along with following custom commands:  
    
    count members  
    count datasets
    cp | copy <arg> arg>    - where first arg is a ".", member or dataset, second arg is dataset
    cancel <arg>            - where arg is a task/job name  
    connections             - a list of connection(s)   
    change <arg>            - where arg is a number representing a connection
    submit <arg>            - where arg is a member name  
    visited                 - a list of visited datasets
  
To quit from the command shell UI, you can either press 'X' windows close icon or enter 'end' keyword.  
  
## Requirements  
  
    Java 11 to execute and build the target jar file.   
    z/OSMF installed on your backend mainframe instance.  
    
## Build And Execute  

Create a creds.txt under the C:\ drive that contains a list of z/OSMF connections per line with a comma delimiter for
connection values. You can specify drive location by changing the hard coded value in the code.  
  
Format:  
    
    hostname,zomsfportnumber,username,password  
    
At the root directory prompt, execute the following maven command:  
  
    mvn clean install  
  
Change directory to the target directory and execute the following command:  
  
    java -jar zosshell-1.0.jar  
  

