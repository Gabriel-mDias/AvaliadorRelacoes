#!/bin/bash

#####################################################################################################
# uso: ./Avaliacao.sh <TIPO_AVALIACAO>
# 
# Dadas uma CD e uma directoria particionada em varias, uma por cada saida, avalia todos os ficheiros 
# .xml que estiverem nessa directoria e produz uma pagina .html de resultados por cada cenario de 
# avaliacao e modo de avaliacao que estiver definido. O nome das directorias com as saidas dos sistemas
# devera ser "partic[0-9][0-9]"
#
# TIPO_AVALIACAO: indica quais as avaliacoes que vao ser executadas, deve ter um dos seguintes valores:
#                 classico: so' faz a avaliacao classica na CD do Segundo HAREM em todos os cenários
#                 tempo:    faz a avalicao classica com CD TEMPO, nos cenários que contém TEMPO, bem como avalia o TEMPO estendido
#                 rerelem:  faz a avaliacao classica com a CD do Segundo HAREM, nos cenários dos sistemas participantes no ReRelEM, bem como avalia o ReRelEM
#                 tudo:     faz todas as avaliacoes anteriores 
#
# Parametros de configuracao do script:
#   cdharem:      nome da CD
#   diravaliacao: directoria raiz onde se encontra a directoria Av_HAREM_XML com os programas.
#   info:         argumento com nome do ficheiro com as informacoes sobre os participantes/saidas.
#   dirinfo:      argumento com nome da directoria onde se encontram os ficheiros .xml para avaliar;
#                 a directoria deve estar dentro de Av_HAREM_XML
#
# Ultima revisao: Cristina Mota, 21.Abril.2010
# copyright: Linguateca
#####################################################################################################


##shopt -s expand_aliases

##alias java16=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Commands/java

##JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.6/Home

### Constantes ###

lib=.:lib/jdom.jar

## Nao e' opcional
estilo="-estilo muc"


## Pistas de avaliacao
classico=0  # HAREM classico
completo=1  # HAREM classico, com pista TEMPO estendido completa
semnorm=2   # HAREM classico, com pista TEMPO estendido sem norm
sonorm=3    # HAREM classico, com pista TEMPO estendido so' com norm
rerelem=4   # HAREM classico, com ReRelEM

## ReRelEM
depara="-t2t1"
converter_notacao="sim"
altrel="sim"

declare -a str_modo
str_modo[0]="classico"
str_modo[1]="estendido_completo"
str_modo[2]="estendido_semnorm"
str_modo[3]="estendido_sonorm"
str_modo[4]="rerelem"

declare -a str_tipo_cenario
str_tipo_cenario[1]="cenario3"
str_tipo_cenario[2]="cenario5"
str_tipo_cenario[3]="total"
str_tipo_cenario[4]="cenario2"
str_tipo_cenario[5]="cenario4"
str_tipo_cenario[6]="cenario6"
str_tipo_cenario[7]="tempo"
str_tipo_cenario[8]="abstraccao"
str_tipo_cenario[9]="acontecimento"
str_tipo_cenario[10]="coisa"
str_tipo_cenario[11]="local"
str_tipo_cenario[12]="obra"
str_tipo_cenario[13]="organizacao"
str_tipo_cenario[14]="pessoa"
str_tipo_cenario[15]="valor"
str_tipo_cenario[16]="pessoaInd"
str_tipo_cenario[17]="pessoaCargo"
str_tipo_cenario[18]="outro"
str_tipo_cenario[19]="pessoaLocalOrg"
str_tipo_cenario[21]="selectivo5Categ"

declare -a str_tipo_cenario_rerelem
str_tipo_cenario_rerelem[1]="todas"
str_tipo_cenario_rerelem[2]="soutra"
str_tipo_cenario_rerelem[3]="ident"
str_tipo_cenario_rerelem[4]="inclusao"
str_tipo_cenario_rerelem[5]="localizacao"

declare -a cenario
cenario[1]="ABSTRACCAO:ACONTECIMENTO:COISA:LOCAL:OBRA:ORGANIZACAO:OUTRO:PESSOA"
cenario[2]="LOCAL(FISICO{*};HUMANO{*})"
cenario[3]="*"
cenario[4]="LOCAL(FISICO{*};HUMANO{*}):PESSOA:ORGANIZACAO:TEMPO"
#cenario[5]="ABSTRACCAO(DISCIPLINA;ESTADO;IDEIA;NOME;OUTRO):ACONTECIMENTO(EFEMERIDE;EVENTO;ORGANIZADO;OUTRO):COISA(CLASSE;MEMBROCLASSE;OBJECTO;OUTRO;SUBSTANCIA):LOCAL(FISICO;HUMANO;VIRTUAL):OBRA(ARTE;OUTRO;PLANO;REPRODUZIDA):ORGANIZACAO(ADMINISTRACAO;EMPRESA;INSTITUICAO;OUTRO):OUTRO:PESSOA(CARGO;GRUPOCARGO;GRUPOIND;GRUPOMEMBRO;INDIVIDUAL;MEMBRO;OUTRO;POVO):TEMPO(DURACAO;FREQUENCIA;GENERICO;OUTRO;TEMPO_CALEND):VALOR(CLASSIFICACAO;MOEDA;OUTRO;QUANTIDADE)"
cenario[5]="ABSTRACCAO:ACONTECIMENTO:COISA:LOCAL:OBRA:ORGANIZACAO:OUTRO:PESSOA:TEMPO:VALOR"
cenario[6]="ACONTECIMENTO(*):LOCAL(*):OBRA(*):ORGANIZACAO(*):OUTRO(*):PESSOA(*):TEMPO(*):VALOR(*)"
cenario[7]="TEMPO"
cenario[8]="ABSTRACCAO"
cenario[9]="ACONTECIMENTO"
cenario[10]="COISA"
cenario[11]="LOCAL"
cenario[12]="OBRA"
cenario[13]="ORGANIZACAO"
cenario[14]="PESSOA"
cenario[15]="VALOR"
cenario[16]="PESSOA(INDIVIDUAL)"
cenario[17]="PESSOA(CARGO)"
cenario[18]="OUTRO"
#cenario[19]="PESSOA(*):LOCAL(*):ORGANIZACAO(*)"
cenario[19]="PESSOA:LOCAL:ORGANIZACAO"
cenario[21]="PESSOA:LOCAL:ORGANIZACAO:TEMPO:VALOR"

declare -a cenario_p
cenario_p[1]=4
cenario_p[3]=14
cenario_p[6]=7
cenario_p[7]=3
cenario_p[9]=1
cenario_p[10]=3
cenario_p[11]=5
cenario_p[13]=2
cenario_p[15]=3
cenario_p[16]=6

declare -a cenario_rerelem
cenario_rerelem[1]=""
cenario_rerelem[2]="-filtro ident;inclui;incluido;sede_de;ocorre_em"
cenario_rerelem[3]="-filtro ident"
cenario_rerelem[4]="-filtro inclui;incluido"
cenario_rerelem[5]="-filtro sede_de;ocorre_em"

declare -a str_alt
str_alt[1]=""
str_alt[2]="altrel"

declare -a proprio
proprio[1]="cenario2"  # Cage2
proprio[3]="pessoa"  # DobrEM
proprio[6]="tempo"  # PorTexTO
proprio[7]="total"  # Priberam
proprio[9]="cenario3"  # R3M
proprio[10]="total"  # REMBRANDT
proprio[11]="cenario4"  # REMMA
proprio[13]="cenario5"  # SEIGeo
proprio[15]="total"  # SeRELeP
proprio[16]="cenario6"  # XIP-L2F/Xerox

cria_proprio(){
    for dir in $dirpartic/partic??; do
	name=${dir/*\/partic0/}
	name=${name/*\/partic/}
	cenario="${proprio[$name]}"
	for file in $dir/*$1*$cenario*$2; do 
	    cp $file "${file/$cenario/proprio}"
	done;    
    done;
}


# ----------------------------
#
# usage: avalia_ficheiro <CENARIO> <MODO>
#
# Invoca a sequencia de programas de avaliacao usando como ponto de partida um ficheiro .xml e uma CD. 
# 
# O argumento <CENARIO> indica o cenario de avaliacao:
#  1 - cenario total
#  [2-8] - indice que indica o cenario selectivo
#
# O argumento MODO especifica o modo de avaliacao:
#  0 - HAREM classico, ou seja, nao faz avaliacao dos atributos estendidos de TEMPO
#  1 - HAREM classico com pista TEMPO completo, ou seja inclui a avaliacao de todos os atributos estendidos de TEMPO
#  2 - HAREM classico com pista TEMPO sem normalizacao, ou seja inclui a avaliacao de TEMPO_REF e SENTIDO
#  3 - HAREM classico com pista TEMPO so' com normalizacao, ou seja inclui a avaliacao de VAL_DELTA e VAL_NORM
#  4 - HAREM classico com pista ReRelEM 
#
#
# A nao ser que seja invocado pelo comando "avalia" e' necessario definir:
#  submissao: nome do ficheiro a avaliar
#  cdharem:   nome da CD
#  diravaliacao: nome da directoria raiz onde se encontra a directoria programas/Av_HAREM_XML 
#                (deve ser uma string com a forma -dir <directoria>) 
#  cenario_avaliacao: opcao que indica o cenario de avaliacao 
#                     (deve ser uma string -avaliacao <descricao_cenario>)
#  cenario_sistema: opcao que indica o cenario de participacao do sistema 
#                   (deve ser uma string -sistema <descricao_cenario>)
# Opcoes especificias para avaliacao do ReRelEM (no caso de ser necessario converter os formatos):
#  submissaot1: nome do ficheiro a avaliar no formato t1 (apenas no caso do ReRelEM)
#  cdharemt1:   nome da CD no formato t1 (apenas no caso do ReRelEM)
# ----------------------------
# $1 - cenario HAREM classico
# $2 - modo/pista
# $3 - altrel?
# $4 - cenario rerelem
# $5 - converter notacao?

avalia_ficheiro(){
#    cd $diravaliacao/programas/Av_HAREM_XML;

    if (( $2==$rerelem && $5==$converter_notacao )); then
	if [ ! -f $cdharemt1 ]; then 
	    java -Dfile.encoding=ISO-8859-1 -Xmx512m -cp $lib pt.linguateca.harem.RerelemChangeNotationM2 $depara $cdharem > $cdharemt1;
	fi;
	cdharem=$cdharemt1
	if [ ! -f $submissaot1 ]; then 
 	    java -Dfile.encoding=ISO-8859-1 -Xmx512m -cp $lib pt.linguateca.harem.RerelemChangeNotationM2 $depara $submissao > $submissaot1
	fi;
	submissao=$submissaot1
    fi;

    if (( $2==$classico )); then
	ficheiro_b=$submissao.${pista}
	ficheiro_c=$ficheiro_b.${str_tipo_cenario[$1]}
    else
	ficheiro_b=$submissao.${pista}_classico
	ficheiro_c=$ficheiro_b.${str_tipo_cenario[$1]}
    fi;
    if (( $2>=$completo && $2<=$sonorm )); then
	ficheiro_b=$submissao.${pista}_${str_modo[$2]}
	ficheiro_e=$ficheiro_b.${str_tipo_cenario[$1]}
    fi;
    if (( $2==$rerelem )); then
	ficheiro_b=$submissao.${pista}
	ficheiro_e=${ficheiro_b}_${str_tipo_cenario_rerelem[$4]}.${str_tipo_cenario[$1]}
    fi;

    echo "CD: " $cdharem
    echo "TST: " $submissao
    echo "OUT: " $ficheiro_c 
    echo "OUT_e: " $ficheiro_e
    set -f
    echo "Cenario de avaliacao: " $cenario_avaliacao
    echo "Cenario de participacao: " $cenario_sistema
    echo "Cenario ReRelEM: " $cenario_rerelem
    set +f 
    if [ -f $ficheiro_c.alinhado.avalida.veu.alts.emir ]; then 
	echo "A versão clássica com avaliação estrista de ALT já existe para o ficheiro $submissao. A avaliação clássica não foi repetida.";
	if [ -f $ficheiro_c.alinhado.avalida.veu.alts.emir.altrel ]; then 
	    echo "A versão clássica com avaliação relaxada de ALT já existe para o ficheiro $submissao. A avaliação clássica não foi repetida.";
	fi;
    else
	if [ -f $ficheiro_b.alinhado.avalida ]; then 
	    echo "Não é necessário fazer alinhamentos com o ficheiro $submissao, porque os alinhamentos com este ficheiro já existe.";
	else
	    java -Xmx512M -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.Aligner -submissao $submissao -cd $cdharem > $ficheiro_b.alinhado;
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.IndividualAlignmentEvaluator -alinhamento $ficheiro_b.alinhado > $ficheiro_b.alinhado.avalida;
	fi;
	set -f
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.AlignmentFilter -alinhamento $ficheiro_b.alinhado.avalida $estilo $cenario_sistema $cenario_avaliacao > $ficheiro_c.alinhado.avalida.veu;
	set +f
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.AltAlignmentOrganizer -alinhamento $ficheiro_c.alinhado.avalida.veu > $ficheiro_c.alinhado.avalida.veu.alts;
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.SemanticAlignmentEvaluator -alinhamento $ficheiro_c.alinhado.avalida.veu.alts $pesos > $ficheiro_c.alinhado.avalida.veu.alts.emir; 
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento $ficheiro_c.alinhado.avalida.veu.alts.emir > $ficheiro_c.alinhado.avalida.veu.alts.emir.ida;
	## Avalicao relaxada de ALT?
	if (( $3==$altrel )); then
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.SemanticAltAlignmentSelector -alinhamento $ficheiro_c.alinhado.avalida.veu.alts.emir > $ficheiro_c.alinhado.avalida.veu.alts.emir.altrel;
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento $ficheiro_c.alinhado.avalida.veu.alts.emir.altrel > $ficheiro_c.alinhado.avalida.veu.alts.emir.altrel.ida;
	fi;
    fi;
    ## Avaliacao Pista TEMPO
    if (( $2>=$completo && $2<=$sonorm )); then                                                                      
	gawk -f AvalTEMPO.awk -v MODO=$2 $ficheiro_c.alinhado.avalida.veu.alts.emir  > $ficheiro_e.alinhado.avalida.veu.alts.emir.tempo;	
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.emir.tempo > $ficheiro_e.alinhado.avalida.veu.alts.emir.tempo.ida;
	## Avalicao relaxada de ALT?
	if (( $3==$altrel )); then
	    gawk -f AvalTEMPO.awk -v MODO=$2 $ficheiro_c.alinhado.avalida.veu.alts.emir.altrel  > $ficheiro_e.alinhado.avalida.veu.alts.emir.altrel.tempo;	
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticEvaluator -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.emir.altrel.tempo > $ficheiro_e.alinhado.avalida.veu.alts.emir.altrel.tempo.ida;
	fi;
    fi;
    ## Avaliacao Pista ReRelEM
    if (( $2==$rerelem )); then 
        echo "==============Arquivo base ReRelEM: alinhamento $ficheiro_c.alinhado.avalida.veu.alts============"
    	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.RelationExpanderM2 -alinhamento $ficheiro_c.alinhado.avalida.veu.alts -exptudo sim > $ficheiro_e.alinhado.avalida.veu.alts.expandido
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.AlignmentSelector -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.IDNormalizer -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.AlignmentsToTriples -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas
        java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.RelationsFilter -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas $cenario_rerelem > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.RelationsEvaluatorM2 -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado.avaliado
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalRelationsEvaluatorM2 -alinhamento $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado.avaliado > $ficheiro_e.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado.avaliado.resumo
    fi;
    rm -f $dirpartic/*.veu $dirpartic/*.expandido $dirpartic/*.selec $dirpartic/*.normalizado $dirpartic/*.triplas $dirpartic/*.filtrado $dirpartic/*.avaliado
    echo "Fim avaliação"
}
 

# ----------------------------
#
# usage: avalia <CENARIO> <MODO>
#
# Invoca a sequencia de programas de avaliacao. 
# 
# O argumento <CENARIO> especifica o cenario de avaliacao. Pode ter um valor numerico de 1 ate' ao limite de cenarios dos arrays $str_tipo_cenario e $cenario:
#  1 - cenario total
#  [2-8] - indice que indica o cenario selectivo
#
# O argumento <MODO> especifica o modo de avaliacao:
#  0 - HAREM classico, ou seja, nao faz avaliacao dos atributos estendidos de TEMPO
#  1 - HAREM classico com pista TEMPO completo, ou seja inclui a avaliacao de todos os atributos estendidos de TEMPO
#  2 - HAREM classico com pista TEMPO sem normalizacao, ou seja inclui a avaliacao de TEMPO_REF e SENTIDO
#  3 - HAREM classico com pista TEMPO so' com normalizacao, ou seja inclui a avaliacao de VAL_DELTA e VAL_NORM
#  4 - HAREM classico com pista ReRelEM 
# ----------------------------
# $1 - cenario
# $2 - modo/pista
# $3 - altrel?
# $4 - cenario rerelem

avalia(){
    cenario_avaliacao="-avaliacao ${cenario[$1]}"
    if (( $2==rerelem )); then
	cenario_rerelem="${cenario_rerelem[$4]}"
    fi;
    for submissao in $dirpartic/partic??/*.xml; do
	name=${submissao/*\/partic0/}; 
	name=${name/*\/partic/}; 
	name=${name/_*/}; 
	cenario_sistema="-sistema ${cenario[${cenario_p[${name}]}]}"
	submissaot1=${submissao/.xml/.t1}
	avalia_ficheiro $1 $2 $3 $4 $5;
    done;
    if (( $2==$classico )); then
	filtro="-filtro ${pista}.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.ida"
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_${pista}_${str_tipo_cenario[$1]}.html;
	if (( $3==$altrel )); then
	    filtro="-filtro ${pista}.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.altrel.ida"
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_altrel_${pista}_${str_tipo_cenario[$1]}.html;
	fi;
    else
	filtro="-filtro ${pista}_classico.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.ida"
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_${pista}_classico_${str_tipo_cenario[$1]}.html;
	if (( $3==$altrel )); then
	    filtro="-filtro ${pista}_classico.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.altrel.ida"
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_altrel_${pista}_classico_${str_tipo_cenario[$1]}.html;
	fi;
    fi;
    if (( $2>=$completo && $2<=$sonorm )); then
	ficheiro_e=$submissao.${pista}_${str_modo[$2]}.${str_tipo_cenario[$1]}

	filtro="-filtro ${pista}_${str_modo[$2]}.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.tempo.ida"
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_${pista}_${str_modo[$2]}_${str_tipo_cenario[$1]}.html;
	if (( $3==$altrel )); then
	    filtro="-filtro ${pista}_${str_modo[$2]}.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.emir.altrel.tempo.ida"
	    java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_altrel_${pista}_${str_modo[$2]}_${str_tipo_cenario[$1]}.html;
	fi;
    fi;
    if (( $2==$rerelem )); then                                                       
	filtro="-filtro $pista_${str_tipo_cenario_rerelem[$4]}.${str_tipo_cenario[$1]}.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado.avaliado.resumo"
	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalRelationsReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_${pista}_${str_tipo_cenario_rerelem[$4]}_${str_tipo_cenario[$1]}.html;
    fi;
}


#diravaliacao=~/data/users/cristina/harem-II/pacoteSegundoHAREM
dirpartic="../../corridas"
dircds="../../coleccoes"
dirinfo="-dir ../../corridas"
info="-info lista_participantes.csv"

mostra_uso(){
    echo "------------------------------------"
    echo "Uso 1: Avaliar participacoes do Segundo HAREM (pre-definida)" 
    echo "       ./Avaliacao.sh {tudo|classico|tempo|rerelem}"
    echo "" 
    echo "Uso 2: Avaliar uma participacao dada uma CD"
    echo "       ./Avaliacao.sh personalizada <participacao> <cd> <cenario_classico> <pista> <altrel?> <cenario_rerelmem> <converter_notacao?> <cenario_participacao>"
    echo ""
    echo "Exemplos: ./Avaliacao.sh tudo"
    echo "          ./Avaliacao.sh personalizada docs/exemplos/exemplo1/ficheiro.xml docs/exemplos/exemplo1/CDexemplo.xml 3 1 sim -1 -1 3"
    echo "------------------------------------"
}

if [ -n "$1" ]; then
    if [[ "$1" != "classico" && "$1" != "tempo" && "$1" != "rerelem" && "$1" != "tudo" && "$1" != "personalizada" ]]; then
	mostra_uso
    fi;
    if [[ "$1" == "classico" || "$1" == "tudo" ]]; then
  	pista="HAREM_Classico"
 	echo $pista
	cdharem=$dircds/CDSegundoHAREMReRelEM.xml
	for (( i=1; i<=15; i++ )); do
	    avalia $i $classico "sim" -1 -1
	done;
 	cria_proprio ${pista} .alinhado.avalida.veu.alts.emir.ida
 	filtro="-filtro ${pista}.proprio.alinhado.avalida.veu.alts.emir.ida"
 	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_${pista}_proprio.html;
 	cria_proprio ${pista} .alinhado.avalida.veu.alts.emir.altrel.ida
 	filtro="-filtro ${pista}.proprio.alinhado.avalida.veu.alts.emir.altrel.ida"
 	java -Dfile.encoding=ISO-8859-1 -cp $lib pt.linguateca.harem.GlobalSemanticReporter -depurar $filtro $dirinfo $info > $dirpartic/resultados_altrel_${pista}_proprio.html;
    fi;
    
    if [[ $1 == "tempo" || $1 == "tudo" ]]; then
	pista="TEMPO"
	echo $pista
	cdharem=$dircds/CDSegundoHAREM_TEMPO.xml
	for (( i=3; i<=7; i++ )); do
	    avalia $i $completo "sim" -1 -1
	    avalia $i $semnorm "sim" -1 -1
	    avalia $i $sonorm "sim" -1 -1
	done;
    fi;
    
    if [[ $1 == "rerelem" || $1 == "tudo" ]]; then
	pista="ReRelEM"
	echo $pista
	cdharem=$3
    cenario_rerelem="${cenario_rerelem[$7]}"
	for (( j=1; j<=5; j++ )); do
	    avalia 3 $rerelem -1 $j "sim";
	    avalia 2 $rerelem -1 $j "sim";
	done;
    fi;

    if [[ $1 == "personalizada" ]]; then
	pista="personalizada"
	cenario_avaliacao="-avaliacao ${cenario[$4]}"
	if (( $5==rerelem )); then
	    cenario_rerelem="${cenario_rerelem[$7]}"
	fi;
	cenario_sistema="-sistema ${cenario[$9]}"
	submissao=$2 
    cdharem=$3
    cdharemt1=${cdharem//.xml/_t1.xml}
    submissaot1=${submissao//.xml/_t1.xml}
    avalia_ficheiro $4 $5 $6 $7 $8
    fi;
    if [[ $1 == "tudo" ]]; then
	./Alcaide.r $dirpartic $dirpartic/relatorios
    fi;
else
    mostra_uso
fi;

# norm_tipos="-normaliza_tipos"
# pesos="-pesos identificação;alfa;beta;gama"
