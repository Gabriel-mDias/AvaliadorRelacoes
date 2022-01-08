#! /Library/Frameworks/R.framework/Versions/2.6/Resources/bin/Rscript --vanilla 

#####################################################################################################
## Usage: ./Alcaide.r
## 
## Description: Dados os ficheiros de resultados em formato html produzidos pelos programas de
##              avaliacao (GlobalSemanticReporter), constroi os relatorios individuais agrupando os
##              resultados referentes 'as varias saidas de um mesmo sistema num mesmo relatorio.
##              Para cada sistema, cria uma directoria com o nome do sistema onde serao colocadas
##              as imagens e o relatorio em formato html.
##              
##
## Last revision: Cristina Mota, 08.Outubro.2008
## Copyright: Linguateca
#####################################################################################################


## Analisa os argumentos fornecidos na linha de comando. Caso nao tenham sido especificados, atribui 
## a results.dir a directoria "participacoes" e a reports.dir "participacoes/relatorios", ambas a
## serem criadas na directoria de onde foi invocado o programa.
##
## ARGUMETOS: alcaide.args - argumentos lidos da linha de comando.
##
## DEVOLVE: Vector com o nome das directorias. 
set.dirs <- function(alcaide.args){
  reports.dir <- paste(getwd(),"/participacoes/relatorios/",sep="") ## "/Users/cmota/data/users/cristina/harem-II/escondido/resultados/relatorios/"
  results.dir <- paste(getwd(),"/participacoes/",sep="") ## "/Users/cmota/data/users/cristina/harem-II/escondido/resultados/"
  
  dresults.idx <- which(alcaide.args=="-dresultados")
  if((length(dresults.idx))>0 && (!is.na(alcaide.args[dresults.idx+1]))){
    results.dir <- alcaide.args[dresults.idx+1]
    if(length(grep("/$",results.dir))==0){
      results.dir <- paste(results.dir,"/",sep="")
    }
  }
  dreports.idx <- which(alcaide.args=="-drelatorios")
  if((length(dreports.idx))>0 && (!is.na(alcaide.args[dreports.idx+1]))){
    reports.dir <- alcaide.args[dreports.idx+1]
    if(length(grep("/$",reports.dir))==0){
      reports.dir <- paste(reports.dir,"/",sep="")
    }
  }
  print(paste("Directoria de resultados:",results.dir))
  print(paste("Directoria de relatórios:",reports.dir))
  return(c(results.dir,reports.dir))
}


## Le a data actual do sistema e devolve a data formato no formato <dia> de <mes_em_portugues> de <ano>
##
## ARGUMENTOS: nenhuns
##
## DEVOLVE: String formatda.
get.date <- function(){
  date.elements <- unlist(strsplit(date()," +"))
  if(date.elements[2] %in% months.en){
    formated.date <- paste(date.elements[3]," de ",months.pt[date.elements[2]]," de ",date.elements[5],", às ",date.elements[4],sep="")
  }
  else{
    formated.date <- paste(date.elements[3],"-",date.elements[2],"-",date.elements[5],", ",date.elements[4],sep="")
  }
  return(formated.date)
}

## Cria um ficheiro html com o indice dos sistemas para os quais se criaram relatorios
##
## ARGUMENTOS: filename - nome do ficheiro html
##
## DEVOLVE: nada.
create.html.index <- function(filename){
  cat("<html LANG=\"pt\">","<head>","<meta http-equiv=Content-Type content=\"text/html; charset=ISO-8859-1\">",
      paste("<title>Relatórios de avaliação no Segundo HAREM</title>",sep=""),
      "<meta http-equiv=\"EXPIRES\" content=\"-1\">",
      "<meta http-equiv=\"PRAGMA\" content=\"NO-CACHE\">",
      "<meta http-equiv=\"MAX-AGE\" content=\"0\">",
      "<meta http-equiv=\"CACHE-CONTROL\" content=\"NO-CACHE\">",
      "</head>",
      "<body>",
      "<h2>Sistemas Participantes no Segundo HAREM</h2>",
      paste("<a href=\"",sys.filenames,"/relatorio_",sys.filenames,"_SegundoHAREM.html\">",sys.names,"</a><br>",sep=""),      
      "</body>",
      "</html>",
      file=filename,sep="\n")
}

## Escreve o cabecalho html do relatorio.
##
## ARGUMENTOS: sys.name - nome do sistema a que se refere o relatorio
##             filename - ficheiro em que se vai escrever o relatorio
## DEVOLVE: nada
write.header <- function(sys.name,filename){
  cat("<html LANG=\"pt\">","<head>","<meta http-equiv=Content-Type content=\"text/html; charset=ISO-8859-1\">",
      paste("<title>Relatório de resultados no Segundo HAREM: sistema ",sys.name,"</title>",sep=""),
      "<meta http-equiv=\"EXPIRES\" content=\"-1\">",
      "<meta http-equiv=\"PRAGMA\" content=\"NO-CACHE\">",
      "<meta http-equiv=\"MAX-AGE\" content=\"0\">",
      "<meta http-equiv=\"CACHE-CONTROL\" content=\"NO-CACHE\">",
      "</head>",
      file=filename,sep="\n")
}

## Escreve a introducao do relatorio
##
## ARGUMENTOS: sys.name - nome do sistema a que se refere o relatorio
##             filename - ficheiro em que se esta' a escrever o relatorio
## DEVOLVE: nada.
write.intro <- function(sys.name,filename){
  cat(paste("<p>Este é o relatório correspondente às saídas oficiais (e/ou não oficiais) do sistema <font color=\"",sys.name.color,"\">",sys.name,"</font> criado pela organização do Segundo HAREM em ",get.date(),", com o programa ",programs.name,", versão ",programs.version,", disponível <a href=\"",programs.URL,"\">aqui</a>.</p>",sep=""),
      file=filename,sep="\n",append=TRUE)
  if( sys.proper.scenarios[sys.name] != "Total"){
    cat(paste("<p>O sistema <font color=\"",sys.name.color,"\">",sys.name,"</font> participou no cenário <b>",sys.proper.scenarios[sys.name],"</b> e foi também avaliado no cenário total e nos cenários dos outros participantes. Todos os cenários estão descritos em <a href=\"http://acdc.linguateca.pt/aval_conjunta/HAREM/DescricaoCenariosSegundoHAREM.html\">Cenários no Segundo HAREM</a>.</p>",sep=""),
        file=filename,sep="\n",append=TRUE)
  }
  else{
    cat(paste("<p>O sistema <font color=\"",sys.name.color,"\">",sys.name,"</font> participou no cenário <b>",sys.proper.scenarios[sys.name],"</b> e foi também avaliado nos cenários dos outros participantes. Todos os cenários estão descritos em <a href=\"http://acdc.linguateca.pt/aval_conjunta/HAREM/DescricaoCenariosSegundoHAREM.html\">Cenários no Segundo HAREM</a>.</p>",sep=""),
        file=filename,sep="\n",append=TRUE)
  }
  cat(paste("<p>As corridas do sistema <font color=\"",sys.name.color,"\">",sys.name,"</font> encontram-se destacadas nos gráficos comparativos com outros sistemas da seguinte forma:",
            "<ul><li>com tamanho maior, no caso de gráficos comparativos precisão/abrangência e também no caso dos gráficos apenas com medida-F;</li>",
            "<li>cores mais claras nos gráficos de barras.</li></ul></p>",sep="\n"),
  file=filename,sep="\n",append=TRUE)

}

## Termina o html do relatorio.
## 
## ARGUMENTOS: sys.name - nome do sistema a que se refere o relatorio
##             filename - ficheiro em que se esta' a escrever o relatorio
## DEVOLVE: nada.
write.footer <- function(sys.name,filename){
  cat("</html>",file=filename,sep="\n",append=TRUE)
}

## Escreve tabela em formato html dados os indices (sy.name, scenario, evaltype e alttype) na estrutura de dados
## que contem os resultados.
##
## ARGUMENTOS: filename - ficheiro em que se esta' a escrever o relatorio
##             results - estrutura com os dados lidos dos ficheiros de resultados
##             sys.name - nome do sistema a que se refere o relatorio
##             scenario - cenario de avaliacao
##             evaltype - tipo de avaliacao (ou seja, se e' Identificacao ou Classificacao)
##             alttype - tipor de avaliacao dos ALT (ou seja, se e' ALT ou ALT_rel)            
## DEVOLVE: nada.
write.table <- function(filename,results,sys.name,scenario,evaltype,alttype){
  results.matrix <- results[[scenario]][[alttype]][[evaltype]]
  runs.names <- names(results.matrix[,"Precisão"])
  sys.rank <- grep(sys.name,runs.names)
  npar <- length(names(results.matrix[1,]))
  cat("<table border=\"1\">",file=filename,sep="\n",append=TRUE)
  cat("<tr bgcolor=\"#cccccc\">",file=filename,sep="\n",append=TRUE)
  cat(paste("<td>Saída</td>",sep=""),
      paste("<td>Posição (em ",length(runs.names),")</td>",sep=""),
      paste(rep("<td>",npar),names(results.matrix[1,]),rep("</td>",npar),sep=""),
      file=filename,sep="",append=TRUE)
  cat("</tr>",file=filename,sep="\n",append=TRUE)
  for(run in sys.rank){
    cat("<tr>",file=filename,sep="\n",append=TRUE)
    cat(paste("<td>",runs.names[run],"</td>",sep=""),
        paste("<td>",run,"</td>",sep=""),
        paste(rep("<td>",npar),format(results.matrix[run,],scientific=FALSE,digits=1,nsmall=4),rep("</td>",npar),sep=""),
        file=filename,sep="",append=TRUE)
    cat("</tr>",file=filename,sep="\n",append=TRUE)
  }
  cat("</table>",
      file=filename,sep="\n",append=TRUE)
}

## Filtra apenas os dados referentes ao sistema alvo do relatorio no que respeita 'a classificacao em modo de avaliacao estrito de ALT.
## Estes dados serao apenas usados na panoramica.
##
## ARGUMENTOS: name - nome do sistema a que se refere o relatorio
##             results - resultados de todos os participantes
##             index - vector com indices a filtrar que podem ser referentes a enarios, categorias ou modos de avaliacao do TEMPO
##
## DEVOLVE: Vector com os dados da classificao com modo de avaliacao estrito de ALT referentes ao sistema alvo do relatorio (passado como argumento).
get.sys.relinfo <- function(name,results,index){
  sys.relresults <- c()
  for(i in index){
    if(length(grep(name,names(results[[i]]$ALT$class[,1]),value=TRUE))>0){
      sys.relresults <- c(sys.relresults,i)
    }
  }
  return(sys.relresults)
}

## Escreve o indice do relatorio.
##
## ARGUMENTOS: filename - ficheiro onde se esta' a escrever o relatorio
##             all.results - resultados de todos os sistemas
##             sys.name - nome do sistema a que se refere o relatorio
##
## DEVOLVE: nada.
write.index <- function(filename,all.results,sys.name){
  sys.scenarios <- get.sys.relinfo(sys.name,all.results,scenarios)
  sys.categories <- get.sys.relinfo(sys.name,all.results,categories)
  sys.scenarios.tempo <- list()
  for(mode.tempo in eval.tempo.short){
    sys.scenarios.tempo[[mode.tempo]] <- get.sys.relinfo(sys.name,all.results,paste(mode.tempo,"_",scenarios.tempo,sep=""))
  }
  cat("<hr>",
      "<ul>",
      "<li><a href=\"#panoramica\">Panorâmica do sistema</a></li>",
      "<ul>",
      "<li><a href=\"#pan_class\">Classificação</a></li>",
      "<ul>",
      paste("<li><a href=\"#class_",overview.short,"\">",overview,"</a></li>\n",sep=""),
      "</ul>",
      "<li><a href=\"#pan_id\">Identificação</a></li>",
      "<ul>",
      paste("<li><a href=\"#id_",overview.short,"\">",overview,"</a></li>\n",sep=""),
      "</ul>",
      "</ul>",
      "<li><a href=\"#classico\">HAREM clássico</a></li>",
      "<ul>",
      "<li><a href=\"#classico_cenarios\">Avaliação por cenários</a></li>",
      "<ul>",
      paste("<li><a href=\"#",sys.scenarios,"\">",scenarios.names[sys.scenarios],"</a>: ",
            "<font size=\"-1\">",
            "<a href=\"#",paste("classico_",sys.scenarios,"_class",sep=""),"\">Classificação</a>, ",
            "<a href=\"#",paste("classico_",sys.scenarios,"_id",sep=""),"\">Identificação</a>",
            "</font>",
            "</li>",sep=""),
      "</ul>",
      "<li><a href=\"#classico_categorias\">Avaliação por categorias</a></li> ",
      "<ul>",
      paste("<li><a href=\"#",sys.categories,"\">",categories.names[sys.categories],"</a>: ",
            "<font size=\"-1\">",
            "<a href=\"#",paste("classico_",sys.categories,"_class",sep=""),"\">Classificação</a>, ",
            "<a href=\"#",paste("classico_",sys.categories,"_id",sep=""),"\">Identificação</a>",
            "</font>",
            "</li>",sep=""),
      "</ul>",
      "</ul>",
      "<li><a href=\"#pista_tempo_estendido\">Pista TEMPO estendido</a></li>",
      "<ul>",
      file=filename,sep="\n",append=TRUE)
  for(mode.tempo in eval.tempo.short){
    cat(paste("<li><a href=\"#",mode.tempo,"\">",eval.tempo[mode.tempo],"</a></li> ",sep=""),
        "<ul>",
        paste("<li><a href=\"#",sys.scenarios.tempo[[mode.tempo]],"\">",
              paste(scenarios.tempo.names[gsub(paste(mode.tempo,"_",sep=""),"",sys.scenarios.tempo[[mode.tempo]])],"</a>: ",
                    "<font size=\"-1\">",
                    "<a href=\"#",paste(sys.scenarios.tempo[[mode.tempo]],"_class",sep=""),"\">Classificação</a>, ",
                    "<a href=\"#",paste(sys.scenarios.tempo[[mode.tempo]],"_id",sep=""),"\">Identificação</a>",
                    "</font>",
                    "</li>",sep=""),sep=""),
        "</ul>",
        file=filename,sep="\n",append=TRUE)
  }
  cat("</ul>",
      "</ul>",
      "<hr>",
      file=filename,sep="\n",append=TRUE)
}

## Cria os varios graficos de desempenho comparativo de um sistema em relacao aos outros para um dado
## cenario e tipo de avaliacao que serao integradas no relatorio.
##
## ARGUMENTOS: results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##             scenario - indice do cenario
##             scenario.name - nome do cenario a colocar no grafico
##             alts - vector que indica o tipo de avaliacao de ALT
##
## DEVOLVE: nada.
create.images <- function(results,sys.name,scenario,scenario.name,alts=alt.types.short){
  filename <- sys.filenames[sys.name]
  results.matrix <- results[[scenario]]
  for(evaltype in eval.types.short){
    for(alttype in alts){
      plot2png(plot.pr,paste(reports.dir,filename,"/",filename,"_",evaltype,"_",alttype,"_",scenario,"_pr.png",sep=""),
               results.matrix[[alttype]][[evaltype]][,"Precisão"]*metrics.norm["P"],
               results.matrix[[alttype]][[evaltype]][,"Abrangência"]*metrics.norm["A"],
               highlight=sys.name,
               title=paste("Perspectiva precisão/abrangência\n[",eval.types[evaltype]," : ",alttype," : ",scenario.name,"]",sep=""),
               mean=TRUE)
      plot2png(barplot.prf,paste(reports.dir,filename,"/",filename,"_",evaltype,"_",alttype,"_",scenario,"_prf.png",sep=""),
               results.matrix[[alttype]][[evaltype]][,"Precisão"]*metrics.norm["P"],
               results.matrix[[alttype]][[evaltype]][,"Abrangência"]*metrics.norm["A"],
               results.matrix[[alttype]][[evaltype]][,"Medida-F"]*metrics.norm["F"],
               highlight=sys.name,
               title=paste("Desempenho ordenado por medida-F\n[",eval.types[evaltype]," : ",alttype," : ",scenario.name,"]",sep=""),
               mean=TRUE)
      for(metric in "F"){
        plot2png(plot.sys,paste(reports.dir,filename,"/",filename,"_",evaltype,"_",alttype,"_",scenario,"_",metric,"_sys.png",sep=""),
                 results.matrix[[alttype]][[evaltype]][,metrics[metric]]*metrics.norm[metric],
                 sys.name,
                 metrics[metric],
                 title=paste("Medida-F agrupada por sistema\n[",eval.types[evaltype]," : ",alttype," : ",scenario.name,"]",sep=""))
      }
    }
  }
}

## Cria os varios graficos de desempenho comparativo de um mesmo sistema em relacao 'a avaliacao por cenarios
## selectivos, categorias e modos de avalicao do TEMPO. Estes graficos serao integrados na panoramica.
##
## ARGUMENTOS: results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##
## DEVOLVE: nada.
create.sys.images <- function(results,sys.name){
  filename <- sys.filenames[sys.name]
  for(evaltype in eval.types.short){
    plot2png(plot.pr2,paste(reports.dir,filename,"/",filename,"_",evaltype,"_ALT_cenarios_pr.png",sep=""),results,scenarios.names[which(scenarios!="proprio")],sys.name,"ALT",evaltype,scenarios.col[which(scenarios!="proprio")],
             title=paste("Comparação de cenários\n[",eval.types[evaltype]," : ALT]",sep=""))
    plot2png(plot.pr2,paste(reports.dir,filename,"/",filename,"_",evaltype,"_ALTrel_cenarios_pr.png",sep=""),results,scenarios.names[which(scenarios!="proprio")],sys.name,"ALTrel",evaltype,scenarios.col[which(scenarios!="proprio")],
             title=paste("Comparação de cenários\n[",eval.types[evaltype]," : ALT rel]",sep=""))
    
    plot2png(plot.pr2,paste(reports.dir,filename,"/",filename,"_",evaltype,"_ALT_categorias_pr.png",sep=""),results,categories.names,sys.name,"ALT",evaltype,categories.col,
             title=paste("Comparação de categorias\n[",eval.types[evaltype],"]",sep=""))
    tempo.names.tmp <- eval.tempo
    eval.tempo.col.tmp <- eval.tempo.col
    names(tempo.names.tmp) <- paste(eval.tempo.short,"_tempo",sep="") 
    names(eval.tempo.col.tmp) <- paste(eval.tempo.short,"_tempo",sep="") 
    plot2png(plot.pr2,paste(reports.dir,filename,"/",filename,"_",evaltype,"_ALT_pistaTEMPO_pr.png",sep=""),
             results,
             tempo.names.tmp,
             sys.name,
             "ALT",
             evaltype,
             eval.tempo.col.tmp,
             title=paste("Comparação de modos na pista TEMPO\n[",eval.types[evaltype]," : cenário TEMPO]",sep=""))
  }
}


## Escreve o html referente a um dado cenario: integra as tabelas e as imagens respectivas.
## 
##
## ARGUMENTOS: results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##             filename - nome do ficheiro html do relatorio
##             scenario - cenario do resultados a serem integrados
##
## DEVOLVE: nada.
write.scenario <- function(results,sys.name,filename,scenario){
  sys.filename <- sys.filenames[sys.name] 
  create.images(results,sys.name,scenario,scenarios.names[scenario])
  cat(paste("<h3><a name=\"",scenario,"\">Cenário ",scenarios.names[scenario],"</a></h3>",sep=""),
      paste("<h4><a name=\"classico_",scenario,"_class\">Classificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,sep="\n",append=TRUE)
  write.table(filename,results,sys.name,scenario,"class","ALT")
  cat(paste("<img src=\"",sys.filename,"_class_ALT_",scenario,"_",img.types,".png\">",img.sep,sep=""),      
      "<h5>Avaliação relaxada de ALT</h5>",
      file=filename,sep="\n",append=TRUE)
  write.table(filename,results,sys.name,scenario,"class","ALTrel")
  cat(paste("<img src=\"",sys.filename,"_class_ALTrel_",scenario,"_",img.types,".png\">",img.sep,sep=""),
      paste("<h4><a name=\"classico_",scenario,"_id\">Identificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,append=TRUE)
  write.table(filename,results,sys.name,scenario,"id","ALT")  
  cat(paste("<img src=\"",sys.filename,"_id_ALT_",scenario,"_",img.types,".png\">",img.sep,sep=""),
      "<h5>Avaliação relaxada de ALT</h5>",
      file=filename,append=TRUE)
  write.table(filename,results,sys.name,scenario,"id","ALTrel")  
  cat(paste("<img src=\"",sys.filename,"_id_ALTrel_",scenario,"_",img.types,".png\">",img.sep,sep=""),
      file=filename,sep="\n",append=TRUE)
}

## Escreve o html referente a uma dada categoria: integra as tabelas e as imagens respectivas.
## 
##
## ARGUMENTOS: results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##             filename - nome do ficheiro html do relatorio
##             category - categoria dos resultados a serem integrados
##
## DEVOLVE: nada.
write.category <- function(results,sys.name,filename,category){
  sys.filename <- sys.filenames[sys.name]
  create.images(results,sys.name,category,categories.names[category],alts="ALT")
  cat(paste("<h3><a name=\"",category,"\">",toupper(category),"</a></h3>",sep=""),
      paste("<h4><a name=\"classico_",category,"_class\">Classificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,sep="\n",append=TRUE)
  write.table(filename,results,sys.name,category,"class","ALT")
  cat(paste("<img src=\"",sys.filename,"_class_ALT_",category,"_",img.types,".png\">",img.sep,sep=""),      
      paste("<h4><a name=\"classico_",category,"_id\">Identificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,append=TRUE)
  write.table(filename,results,sys.name,category,"id","ALT")  
  cat(paste("<img src=\"",sys.filename,"_id_ALT_",category,"_",img.types,".png\">",img.sep,sep=""),
      file=filename,sep="\n",append=TRUE)
}

## Escreve o html referente a um dado modo de avaliacao do TEMPO e cenario: integra as tabelas e as imagens respectivas.
## 
##
## ARGUMENTOS: results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##             filename - nome do ficheiro html do relatorio
##             mode - modo de avaliacao do TEMPO dos resultados a serem integrados
##             scenario - cenario dos resultados a serem integrados
##
## DEVOLVE: nada.
write.scenario.tempo <- function(results,sys.name,filename,mode,scenario){
  sys.filename <- sys.filenames[sys.name]
  tempo.name <- paste(mode,"_",scenario,sep="")
  create.images(results,sys.name,tempo.name,paste(mode,"_",scenarios.tempo.names[scenario],sep=""),alts="ALT")
  cat(paste("<h3><a name=\"",tempo.name,"\">Cenário ",scenarios.tempo.names[scenario],"</a></h3>",sep=""),
      paste("<h4><a name=\"",tempo.name,"_class\">Classificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,sep="\n",append=TRUE)
  write.table(filename,results,sys.name,tempo.name,"class","ALT")
  cat(paste("<img src=\"",sys.filename,"_class_ALT_",tempo.name,"_",img.types,".png\">",img.sep,sep=""),      
      paste("<h4><a name=\"",tempo.name,"_id\">Identificação</a></h4>",sep=""),
      "<h5>Avaliação estrita de ALT</h5>",
      file=filename,append=TRUE)
  write.table(filename,results,sys.name,tempo.name,"id","ALT")  
  cat(paste("<img src=\"",sys.filename,"_id_ALT_",tempo.name,"_",img.types,".png\"><br>",img.sep,sep=""),
      file=filename,sep="\n",append=TRUE)
}


## Cria o relatorio de um dado sistema.
## 
##
## ARGUMENTOS: all.results - resultados de todos os sistemas 
##             sys.name - nome do sistema a que se refere o relatorio
##             filename - nome do ficheiro html do relatorio
##
## DEVOLVE: nada.
output.report <- function(all.results,sys.name,filename){
  sys.filename <- sys.filenames[sys.name]
  create.sys.images(results,sys.name)
  write.header(sys.name,filename)
  cat("<body>",
      paste("<h1>Relatório de avaliação no Segundo HAREM: sistema <font color=\"",sys.name.color,"\">",sys.name,"</font></h1>",sep=""),
      file=filename,sep="\n",append=TRUE)
  write.intro(sys.name,filename)
  write.index(filename,all.results,sys.name)
  cat("<h2><a name=\"panoramica\">Panorâmica</a></h2>",file=filename,sep="\n",append=TRUE)
  for(evaltype in eval.types.short){
    cat(paste("<h3><a name=\"pan_",evaltype,"\">",eval.types[evaltype],"</a></h3>",sep=""),file=filename,sep="\n",append=TRUE)
    cat(paste("<a name=\"",evaltype,"_comp_cenarios_alt\"><img src=\"",sys.filename,"_",evaltype,"_ALT_cenarios_pr.png",sep=""),"\"></a>",file=filename,sep="\n",append=TRUE)
    cat(paste("<a name=\"",evaltype,"_comp_cenarios_altrel\"><img src=\"",sys.filename,"_",evaltype,"_ALTrel_cenarios_pr.png",sep=""),"\"></a><br>",file=filename,sep="\n",append=TRUE)
    cat(paste("<a name=\"",evaltype,"_comp_categorias\"><img src=\"",sys.filename,"_",evaltype,"_ALT_categorias_pr.png",sep=""),"\"></a>",file=filename,sep="\n",append=TRUE)
    cat(paste("<a name=\"",evaltype,"_comp_modos\"><img src=\"",sys.filename,"_",evaltype,"_ALT_pistaTEMPO_pr.png",sep=""),"\"></a><br>",file=filename,sep="\n",append=TRUE)
  }
  cat("<hr>",
      "<h2><a name=\"classico\">HAREM Clássico</a></h2>",
      "<h3><a name=\"classico_cenarios\">Avaliação por cenários</a></h3>",
      file=filename,sep="\n",append=TRUE)
  for(scenario in scenarios){
    if(length(grep(sys.name,names(all.results[[scenario]]$ALT$class[,1]),value=TRUE))>0){
      write.scenario(all.results,sys.name,filename,scenario)
    }
  }
  cat("<h3><a name=\"classico_categorias\">Avaliação por categorias</a></h3>",file=filename,sep="\n",append=TRUE)
  for(category in categories){
    if(length(grep(sys.name,names(all.results[[category]]$ALT$class[,1]),value=TRUE))>0){
      write.category(all.results,sys.name,filename,category)
    }
  }
  cat("<h2><a name=\"pista_tempo_estendido\">Pista TEMPO</a></h2>",
      file=filename,sep="\n",append=TRUE)
  for(mode.tempo in eval.tempo.short){
    cat(paste("<h2><a name=\"",mode.tempo,"\">",eval.tempo[mode.tempo],"</a></h2>",sep=""),
        file=filename,sep="\n",append=TRUE)
    for(scenario in scenarios.tempo){
      if(length(grep(sys.name,names(all.results[[paste(mode.tempo,"_",scenario,sep="")]]$ALT$class[,1]),value=TRUE))>0){
        write.scenario.tempo(all.results,sys.name,filename,mode.tempo,scenario)
      }
    }
  }
  cat("</body>",
      file=filename,sep="\n",append=TRUE)
  write.footer(sys.name,filename)
}

## Escreve o grafico, cuja funcao que o desenha e' enviada como argumento, num ficheiro png. 
## 
##
## ARGUMENTOS: f - funcao que desenha o grafico
##             filename - nome do ficheiro png onde vai ser gravado o grafico
##             ... - dados que vao ser usados para criar o grafico
##
## DEVOLVE: nada.
plot2png <- function(f,filename,...){
  f(...)
  png(filename)
  f(...)
  dev.off()
}

## Desenha um grafico Precisao como funcao da Abrangencia comparativo das varias saidas de um mesmo sistema. 
## 
##
## ARGUMENTOS: results - todos os resultados
##             ori.index.names - legenda dos resultados
##             sys.name - nome do sistema a que diz respeito o relatorio
##             alttype - tipo de avaliacao de ALT
##             evaltype - tipo de avaliacao (identificacao ou classificaca)
##             ori.index.col - indice das cores
##             title - titulo do grafico
##
## DEVOLVE: nada.
plot.pr2 <- function(results,ori.index.names,sys.name,alttype,evaltype,ori.index.col,title=""){
  precision <- c()
  recall <- c()
  index <- c()
  index.col <- c()
  index.pch <- c()
  for(idx in names(ori.index.names)){
    runs.names <- sort(grep(sys.name,names(results[[idx]][[alttype]][[evaltype]][,metrics["P"]]),value=TRUE))
    if(length(runs.names)>0){
      leg.runs.names <- runs.names
      precision <- c(precision,results[[idx]][[alttype]][[evaltype]][runs.names,metrics["P"]])
      recall <- c(recall,results[[idx]][[alttype]][[evaltype]][runs.names,metrics["A"]])
      index.col <- c(index.col,rep(ori.index.col[idx],length(runs.names)))
      index <- c(index,paste(idx,runs.names))
      index.pch <- c(index.pch,sys.pch[runs.names])
    }
  }
  if(length(index)>0){
    precision <- precision *metrics.norm["P"]
    recall <- recall *metrics.norm["A"]
    
    grid.v <- 0:10 * 0.1
    grid.h <- 0:10 * 0.1
    
    plot(NA, NA, type = "n",xlab="Abrangência",ylab="Precisão",main=title,xlim=c(0,1),ylim=c(0,1))
    abline(v = grid.v, h = grid.h, col = "lightgray", lty = "dotted")
    abline(v = mean(recall), h = mean(precision), col = "black", lty = "dotted")
    
    points(recall,precision,col=index.col,pch=index.pch)
    legend(0.7, 1.02, c(ori.index.names,leg.runs.names),ncol=1,col=c(ori.index.col,rep("grey25",length(leg.runs.names))),pch=c(rep(NA,length(ori.index.names)),sys.pch[leg.runs.names]),cex=0.75,text.col=c(ori.index.col,rep("grey25",length(leg.runs.names)))) # bg="white"
  }
}

## Desenha um grafico Precisao como funcao da Abrangencia comparativo dos varias saidas dos sistemas
## participantes.
## 
##
## ARGUMENTOS: precision - vector de valores de precisao
##             recall - vector de valores de abrangencia
##             highlight - nome do sistema a salientar no grafico
##             title - titulo do grafico
##             mean - se for TRUE inclui no grafico a media da precisao e tambem da abrangencia
##
## DEVOLVE: nada.
plot.pr <- function(precision,recall,highlight="",title="",mean=FALSE){
  grid.v <- 0:10 * 0.1
  grid.h <- 0:10 * 0.1

  plot(NA, NA, type = "n",xlab="Abrangência",ylab="Precisão",main=title,xlim=c(0,1),ylim=c(0,1))
  abline(v = grid.v, h = grid.h, col = "lightgray", lty = "dotted")
  abline(v = mean(recall), h = mean(precision), col = "black", lty = "dotted")
  sys.cex <- rep(1.5,length(sys.runs))
  names(sys.cex) <- sys.runs
  if(highlight != ""){
    sys.cex[grep(highlight,sys.runs)] <- 3
  }
  runs.rank=names(precision)
  points(recall,precision,col=sys.fg[runs.rank],bg=sys.bg[runs.rank],pch=sys.pch[runs.rank],cex=sys.cex[runs.rank])
  points(mean(recall),mean(precision),col="yellow",bg="orange",pch=9,cex=3)
  legend(0.7, 1.02, c(sys.runs,"MÉDIA"),ncol=1,col=c(sys.fg,"yellow"),pt.bg=c(sys.bg,"orange"),pch=c(sys.pch,9),cex=0.75) # bg="white"
}

## Desenha um grafico de barras com Precisao, Abrangencia e Medida-F lado a lado comparativo dos varias saidas
## dos sistemas participantes.
## 
##
## ARGUMENTOS: precision - vector de valores de precisao
##             recall - vector de valores de abrangencia
##             f.measure - vector de valores de medida-f
##             highlight - nome do sistema a salientar no grafico
##             title - titulo do grafico
##             mean - se for TRUE inclui no grafico a media da precisao e tambem da abrangencia
##
## DEVOLVE: nada.
barplot.prf <- function(precision,recall,f.measure,highlight="",title="",mean=FALSE){
  grid.h <- 0:10 * 0.1
  bar.col=rep(c("dark red","dark green","dark blue"),length(precision))
  barplot(matrix(NA,3,length(precision)),beside=TRUE,col=bar.col,names.arg=names(precision), axes=FALSE, axisnames=FALSE,ylim=c(0,1),cex.names=0.75) 
  if(highlight != ""){
    bar.col[sapply((grep(highlight,names(precision))-1)*3, "+",c(1,2,3))] <- c("red","green","blue")
  }
  abline(h = grid.h, col = "lightgray", lty = "dotted")
  abline(h = mean(precision), col = "red", lty = "dotted")
  abline(h = mean(recall), col = "green", lty = "dotted")
  abline(h = mean(f.measure), col = "blue", lty = "dotted")
  barplot(matrix(c(precision,recall,f.measure),3,length(precision),byrow=TRUE),beside=TRUE,col=bar.col,names.arg=names(precision), main=title,ylim=c(0,1),add=TRUE,las=3,cex.names=0.75)
  legend(1, 1, c(metrics,paste(metrics," de ",highlight)),ncol=2,col=c("dark red","dark green","dark blue","red","green","blue"),pt.bg=c("dark red","dark green","dark blue","red","green","blue"),pch=22,cex=0.75) # bg="white"
  text(rep(length(precision)*4,3),c(mean(precision),mean(recall),mean(f.measure)),paste(metrics,"MÉDIA"),col=c("red","green","blue"),adj=c(1,0),cex=0.75)
}

## Desenha um grafico comparativo de uma dada metrica em funcao dos sistemas (ou seja, agrupa as saidas de um mesmo
## sistema, colocando os varios grupos lado a lado).
## 
##
## ARGUMENTOS: metric.runs - vector de valores de uma dada medida
##             highlight - nome do sistema a destacar no grafico
##             metric.name - nome da medida que esta' a ser desenhada
##             title - titulo do grafico
##
## DEVOLVE: nada.
plot.sys <- function(metric.runs,highlight="",metric.name="",title=""){
  grid.h <- 0:10 * 0.1

  plot(NA,NA, type = "n",ylab=metric.name,main=title,ylim=c(0,1),xlim=c(1,length(sys.names)),xaxt="n",xlab="Sistemas")
  abline(h = grid.h, col = "lightgray", lty = "dotted")
  abline(h = mean(metric.runs), col = "black", lty = "dotted")
  sys.cex <- rep(1.5,length(sys.runs))
  names(sys.cex) <- sys.runs
  if(highlight != ""){
    sys.cex[grep(highlight,sys.runs)] <- 3
  }
  runs.rank=names(metric.runs)
  points(sys.pos[runs.rank],metric.runs,col=sys.fg[runs.rank],bg=sys.bg[runs.rank],pch=sys.pch[runs.rank],cex=sys.cex[runs.rank])
  axis(1,at=1:length(sys.names),labels=sys.names,las=3,cex.axis=0.75)
  text(length(sys.names),mean(metric.runs),"Medida-F MÉDIA",col="black",adj=c(1,0),cex=0.75)
}


## Le os resultados a partir de um ficheiro.
## 
##
## ARGUMENTOS: filename - nome do ficheiro de resultados
##
## DEVOLVE: nada.
read.results <- function(filename){
  results <- scan(file(paste(results.dir,filename,sep=""),encoding="iso-8859-1"),what="char",sep="\n")
  results <- grep("<tr><td>",results,value=TRUE)
  results <- gsub("</?[^<>]+>"," ",results)
  results <- strsplit(results," +")
  
  system.runs <- grep("^[A-Za-z]+",unlist(results),value=TRUE)
  values <- sub(",",".",grep("^[0-9]+",unlist(results),value=TRUE))
  
  n.partic <- length(system.runs)/2
  results.matrix <- list()
  results.matrix[["class"]] <- matrix(as.numeric(values[1:(n.partic*eval.type.dim["class"])]),
                                      n.partic,
                                      eval.type.dim["class"],
                                      dimnames=list(system.runs[1:n.partic],metric.names[["class"]]),byrow=TRUE)

  results.matrix[["id"]] <- matrix(as.numeric(values[(1+n.partic*eval.type.dim["class"]):length(values)]),
                                   n.partic,
                                   eval.type.dim["id"],
                                   dimnames=list(system.runs[(1+n.partic):(2*n.partic)],metric.names[["id"]]),byrow=TRUE)
  results.matrix
}

## Le todos os ficheiros html de resultados
## 
##
## ARGUMENTOS: nao tem argumentos.
##
## DEVOLVE: nada.
read.all.results <- function(){
  results <- list()
  for(scenario in scenarios){
    results[[scenario]] <- list()
    for(alttype in alt.types.short){
      results[[scenario]][[alttype]] <- read.results(paste(alt.types[alttype],"_HAREM_Classico_",scenario,".html",sep=""))
    }
  }

  for(category in categories){
    results[[category]] <- list()
    results[[category]][["ALT"]] <- read.results(paste(alt.types["ALT"],"_HAREM_Classico_",category,".html",sep=""))
  }

  for(scenario in scenarios.tempo){
    results[[paste("tempo_classico_",scenario,sep="")]][["ALT"]] <- read.results(paste(alt.types["ALT"],"_TEMPO_classico_",scenario,".html",sep=""))
    for(mode.t in modes.tempo){
      results[[paste("tempo_",mode.t,"_",scenario,sep="")]][["ALT"]] <- read.results(paste(alt.types["ALT"],"_TEMPO_",mode.t,"_",scenario,".html",sep=""))
    }
  }
  
  for(category in categories){
    results[[category]] <- list()
    results[[category]][["ALT"]] <- read.results(paste(alt.types["ALT"],"_HAREM_Classico_",category,".html",sep=""))
  }

  return(results)
}


## Gera os relatorios respeitantes aos nomes de ficheiro passados como argumento.
## 
##
## ARGUMENTOS: sys.names - nomes dos ficheiros para os quais se vai gerar relatorios
##             results - resultados de todos os cenarios, categorias e modos de avaliacao do TEMPO 
##
## DEVOLVE: nada.
generate.reports <- function(sys.names,results){
  for(sys.name in sys.names){
  print(paste("A criar o relatório do sistema ",sys.name,"...",sep=""))
  system(paste("mkdir -p ",reports.dir,sys.filenames[sys.name],sep=""))
  sys.file <- file(paste(reports.dir,sys.filenames[sys.name],"/relatorio_",sys.filenames[sys.name],"_SegundoHAREM.html",sep=""),open="w",encoding="iso-8859-1")
  output.report(results,sys.name,sys.file)
  close(sys.file)
}
  
  reports.index <- file(paste(reports.dir,"index.html",sep=""),open="w",encoding="iso-8859-1")
  create.html.index(reports.index)
}

## Definicao de variaveis

alcaide.args <- commandArgs(TRUE)

# Dados sobre os programas a serem escritos na introducao do relatorio
programs.name <- "Alcaide.r" 
programs.version <- "2.0"
programs.URL <- "http://acdc.linguateca.pt/aval_conjunta/HAREM/avaliacao/Av_HAREM_XML.zip"

# Titulos e dados para as seccoes da panamorica
overview <- c("Comparação de cenários com avaliação estrita de ALT",
              "Comparação de cenários com avaliação relaxada de ALT",
              "Comparação de categorias",
              "Comparação de modos na pista TEMPO")
overview.short <- c("comp_cenarios_alt","comp_cenarios_altrel","comp_categorias","comp_modos")
names(overview) <- overview.short

# Cor a ser usada para destacar o nome do sistema nos relatorios
sys.name.color <- "red"

# Nomes dos meses em ingles e portugues para fazer a conversao caso os nomes estejam em ingles
months.en <- c("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
months.pt <- c("Janeiro","Fevereiro","Março","Abril","Maio","Junho","Julho","Agosto","Setembro","Outubro","Novembro","Dezembro")
names(months.pt) <- months.en

# Nomes das categorias usadas na avaliacao e dados de formatacao dos graficos referentes 'as mesmas
categories <- c("abstraccao","acontecimento","coisa","local","obra","organizacao","pessoa","tempo","valor")
categories.names <- toupper(categories)
categories.col <- c("red","blue","green","black","orange","brown","magenta","cyan","grey50")
names(categories.col) <- categories
names(categories.names) <- categories

# Nomes dos cenarios do HAREM classico e dados de formatacao dos graficos referentes aos mesmos
scenarios <- c("total","proprio",paste("cenario",2:6,sep=""))
scenarios.names <- c("Total","Selectivo",paste("Selectivo ",2:6,sep=""))
scenarios.col <- c("red","blue","green","black","orange","brown","magenta")
names(scenarios.col) <- scenarios
names(scenarios.names) <- scenarios

# Nomes dos cenarios do TEMPO estendido e dados de formatacao dos graficos referentes aos mesmos
scenarios.tempo <- c("total","tempo",paste("cenario",c(2,4,6),sep=""))
scenarios.tempo.names <- c("Total","TEMPO",paste("Selectivo ",c(2,4,6),sep=""))
scenarios.tempo.col <- c("red","blue","green","black","orange")
names(scenarios.tempo.col) <- scenarios.tempo
names(scenarios.tempo.names) <- scenarios.tempo

eval.tempo <- c("HAREM clássico na miniCD TEMPO","TEMPO estendido completo","TEMPO estendido sem normalização","TEMPO só com normalização")
eval.tempo.short <- c("classico","completo","semnorm","sonorm")
names(eval.tempo) <- eval.tempo.short
eval.tempo.col <- c("red","blue","green","magenta")
names(eval.tempo.col) <- eval.tempo.short

complete.scenarios.tempo <- paste(rep(eval.tempo.short,each=length(scenarios.tempo)),"_",scenarios.tempo,sep="")
complete.scenarios.tempo.names <- paste(rep(eval.tempo,each=length(scenarios.tempo))," ",scenarios.tempo,sep="")

modes.tempo <- c("estendido_completo","estendido_semnorm","estendido_sonorm")
names(modes.tempo) <- eval.tempo.short[2:4]

# Tipos de pontuacao 
eval.types <- c("Classificação","Identificação")
eval.types.short <- c("class","id")
names(eval.types) <- eval.types.short

# Modos de avaliacao de ALT
alt.types <- c("resultados","resultados_altrel")
alt.types.names <- c("Avaliação estrita de ALT","Avaliação relaxada de ALT")
alt.types.short <- c("ALT","ALTrel")
names(alt.types) <- alt.types.short
names(alt.types.names) <- alt.types.short

# Nomes das metricas de avaliacao
metrics.short <- c("P","A","F")
metrics <- c("Precisão","Abrangência","Medida-F")
names(metrics) <- metrics.short

# Vector de conversoes das medidas; sera 1 se nao for para efectuar conversoes ou 100 no caso de se pretender ter percentagens
metrics.norm <- c(1,1,1)
names(metrics.norm) <- metrics.short

# Dados que indicam o sufixo a adicionar ao nome dos ficheiros de imagens e os separadores entre figuras
img.types <- c("pr","prf",paste("F","_sys",sep=""))
img.sep <- c("","<br>","<br>")

# Nomes dos sistemas participantes
sys.names=c("Cage2",
  "DobrEM",
  "PorTexTO",
  "Priberam",
  "R3M",
  "REMBRANDT",
  "REMMA",
  "SEIGeo",
  "SeRELeP",
  "XIP-L2F/Xerox")

# Nomes dos sistemas para efeitos de manipulacao de ficheiros/directorias
sys.filenames <- gsub("/","_",sys.names)
names(sys.filenames) <- sys.names

# Nomes dos cenarios de avaliacao; tem de corresponder aos nomes usados nos ficheiros
sys.proper.scenarios=c("Selectivo2",
  "PESSOA",
  "TEMPO",
  "Total",
  "Selectivo3",
  "Total",
  "Selectivo4",
  "Selectivo5",
  "Total",
  "Selectivo6")

# Nomes das corridas; tem de corresponder aos nomes que estao nas tabelas de resultados
sys.runs=c("Cage2_1_corr","Cage2_2_corr","Cage2_3_corr","Cage2_4_corr",
  "DobrEM_1_corr",
  "PorTexTO_1_corr","PorTexTO_2_corr","PorTexTO_3_corr","PorTexTO_4_corr",
  "Priberam_1",
  "R3M_1","R3M_2",
  "REMBRANDT_1","REMBRANDT_2","REMBRANDT_3_corr",
  "REMMA_1_corr","REMMA_2_corr","REMMA_3_corr",
  "SEIGeo_1","SEIGeo_2","SEIGeo_3","SEIGeo_4",
  "SeRELeP_1","SeRELeP_no",
  "XIP-L2F/Xerox_1","XIP-L2F/Xerox_2","XIP-L2F/Xerox_3","XIP-L2F/Xerox_4","XIP-L2F/Xerox_no")

# Indices das corridas em termos de sistemas (ou seja, em relacao ao indice de nomes de sistemas, sys.names)
  sys.pos=c(rep(1,4),
  rep(2,1),
  rep(3,4),
  rep(4,1),
  rep(5,2),
  rep(6,3),
  rep(7,3),
  rep(8,4),
  rep(9,2),
  rep(10,5))

# Cores de fundo dos sistemas para os marcadores nos graficos; cada linha corresponde a um sistema diferente
sys.bg=c(rep("grey50",4),
  rep("blue",1),
  rep("green",4),
  rep("magenta",1),
  rep("red",2),
  rep("white",3),
  rep("white",3),
  rep("white",4),
  rep("white",2),
  rep("white",5))

# Cores do contorno dos sistemas para os marcadores nos graficos; cada linha corresponde a um sistema diferente
sys.fg=c(rep("black",4),
  rep("black",1),
  rep("black",4),
  rep("black",1),
  rep("black",2),
  rep("grey50",3),
  rep("blue",3),
  rep("green",4),
  rep("magenta",2),
  rep("red",5))

# Forma dos marcadores nos graficos; cada linha corresponde a um sistema diferente
sys.pch=c(21,22,23,24,
  21,
  21,22,23,24,
  21,
  21,22,
  21,22,23,
  21,22,23,
  21,22,23,24,
  21,22,
  21,22,23,24,25)

# Associcao entre nomes das corridas e os dados de formatacao
names(sys.proper.scenarios) <- sys.names
names(sys.fg) <- sys.runs
names(sys.bg) <- sys.runs
names(sys.pch) <- sys.runs
names(sys.pos)  <- sys.runs

# Cabecalhos das tabelas de resultados; cada ficheiro de resultados contem 2 tabelas (dai uma lista com dois elementos)
metric.names <- list(c("Precisão","Abrangência","Medida-F","MaxCD","MaxSis"),
                     c("Precisão","Abrangência","Medida-F","Sobre-ger","Sub-ger","TotalEMCD","TotalEMSis"))
names(metric.names) <- eval.types.short
eval.type.dim <- c(5,7)
names(eval.type.dim) <- eval.types.short

# Definicao do nomes das directorias de onde vao ler lidos os ficheiros de resultados e onde vao ser escritos os relatorios individuais
dirs <- set.dirs(commandArgs(TRUE))
results.dir <- dirs[1]
reports.dir <- dirs[2]

# Leitura de todos os ficheiros de resultados
results <- read.all.results()
# Geracao dos relatorios individuais
generate.reports(sys.names,results)

