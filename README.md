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
    cancel <arg> - where arg is a task name  
  
To quit from the command shell UI, you can either press ctrl-c keys or enter 'end' keyword.  
  
