#!/bin/bash

grafo=$1

#Se o usuário passou uma coleção específica, copia os textos dessa coleção (que estão na pasta Entrada_<nome_da_colecao>) para pasta Entrada e a lista de arquivos correspondentes para listaArquivos.txt
if [ -n "$2" ]; then
	rm -r Entrada
	cp -r Entrada_$2 Entrada
	rm listaArquivos.txt
	cp listaArquivos_$2.txt listaArquivos.txt
fi

rm -r Entrada1
mkdir Entrada1

cd Entrada
for i in *txt
do
	#Faz preprocessamento da base. Exclui o - porque no livro A Senhora tinham vários - no meio das strings, inclusive dos nomes. Exclui *
	#pq dá problemas na hora de rotular nomes.
	#sed -i 's/"//g' $i 
	sed " s/amp;//g; s/<\/P>/./g; s/<[^>]*>//g; s/'//g; s/\"//g; s/«//g; s/^[..]*//g; s/^[...]*//g; s/^[- ]*//g; s/{//g; s/• //g; s/•//g; s;\r;;g; s;*;;g" $i > ../Entrada1/$i
done
cd ..

tipo=${grafo##*.}
if [ "$tipo" = "csc" ]
then
	./2-AplicaCascata.sh $grafo
else
	./2-AplicaGrafo.sh $grafo	
fi

./3-RotulaNomesColecao.sh

./4-ConcatenaArquivos.sh $2	#Colecao é variável recebida do script ExecutaGrafo3Colecoes


if [ -n "$2" ]; then

	avaliacao=5

    avaliacaoRelRelem=5
    #Para cenários mais específicos na avaliação de relação, modifique o parâmetro
        #1 - Genérico
        #2 - ident;inclui;incluido;sede_de;ocorre_em;
        #3 - ident;
        #4 - inclui;incluido;
        #5 - sede_de;ocorre_em;
        #6 - relacao_familiar"
        #7 - ident;inclui;incluido;sede_de;ocorre_em;relacao_familiar"

	#Faz avaliação usando programa do segundo HAREM e obtém os resultados
	cp CD$2_anotado.xml RecursosSegundoHarem/programas/Av_HAREM_XML/Experimentos/$2/Participacao$2.xml

	cd RecursosSegundoHarem/programas/Av_HAREM_XML

    #Nova avaliação
    ./Avaliacao.sh personalizada Experimentos/$2/Participacao$2.xml Experimentos/$2/$2.xml $avaliacao 4 sim $avaliacaoRelRelem sim $avaliacao

	case "$avaliacao" in
		"5") cenario="cenario4";;
		"11") cenario="local";;
		"13") cenario="organizacao";;
		"16") cenario="pessoaInd";;
		"17") cenario="pessoaCargo";;
		"18") cenario="outro";;
	esac
    
        #Resultado da etiquetagem
    iconv -f iso-8859-1 -t utf-8 Experimentos/$2/Participacao$2_t1.xml.personalizada_classico.$cenario.alinhado.avalida.veu.alts.emir.altrel.ida > Resultado_$2.txt

        #Resultado das relações (Obs: O arquivo que está definido é para o cenário 1. Se udar de cenário de relações, redirecione o arquivo para o adequado)
	iconv -f iso-8859-1 -t utf-8 Experimentos/$2/Participacao$2_t1.xml.personalizada_todas.cenario4.alinhado.avalida.veu.alts.expandido.selec.normalizado.triplas.filtrado.avaliado.resumo > Resultado_$2_ReRelEM.txt

    P=`cat Resultado_$2.txt | grep "Precisão Máxima do Sistema:" | cut -d":" -f2 | sed 's/^ \+//'`	
	A=`cat Resultado_$2.txt | grep "Abrangência Máxima na CD:" | cut -d":" -f2 | sed 's/^ \+//'`
	F=`cat Resultado_$2.txt | grep "Medida F:" | tail -1 | cut -d":" -f2 | sed 's/^ \+//'`

    RP=`cat Resultado_$2_ReRelEM.txt | head -31 | tail -1 | grep "Precisão:" | cut -d":" -f2 | sed 's/^ \+//'`	
	RA=`cat Resultado_$2_ReRelEM.txt | head -32 | tail -1 | grep "Abrangência:" | cut -d":" -f2 | sed 's/^ \+//'`
	RF=`cat Resultado_$2_ReRelEM.txt | head -33 | tail -1 | grep "Medida F:" | tail -1 | cut -d":" -f2 | sed 's/^ \+//'`
	
    echo "$2;$P;$A;$F;$RP;$RA;$RF;" >> Resultados.xls			

	echo "" >> Resultados.xls
	cp Resultados.xls ../../../Resultados.xls

	rm Resultado_$2.txt
    rm Resultado_$2_ReRelEM.txt

    #TODO: Remover comentário	
    #rm Experimentos/$2/Participacao*
    #rm Experimentos/$2/*_t1.xml

fi

