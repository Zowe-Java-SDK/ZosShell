# ZosShell

This project demostrates the usage of the Zowe Java SDK located here https://github.com/zowe/zowe-client-java-sdk.    
  
Project provides a UI shell command prompt that allows you to manipulate datasets and its members. The shell performs linux like commands:  
  
    cat
    cd
    ls  
    ls -l
    ps
    ps <arg> - where arg is a task/job name   
    pwd   
  
Along with following custom like commands:  
    
    count members  
    count datasets  
    visited  
    submit <arg> - where arg is a member name  
    cancel <arg> - where arg is a task name  
  
To quit from the command shell UI, you can either ctrl-c or enter 'end' keyword.  
  
