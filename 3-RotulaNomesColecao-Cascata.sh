#!/bin/bash

cd Entrada1

for arq in *.txt
do

	arqSemExt="${arq%.*}"	#Nome do arquivo sem extensão

	sed 's;{S};;g; s;\\.;.;g' $arq > $arqSemExt1".txt"

	numId=1

	# Reconhece e anota expressões com categoria e tipo. Se quiser só categoria ou tipo e subtipo, tenho que acrescentar novas linhas semelhantes a essa para reconhecê-las e anotá-las
    # Necessário criar um ID específico
	perl -pe 's/{(.*?),.(.*?)\+(.*?)}/"<EM ID=\"'$arqSemExt'-".($numId++)(int(rand(1000)))."\" CATEG=\"$2\" TIPO=\"$3\">$1<\/EM>"/eg' $arqSemExt1".txt" > $arqSemExt"_anotado1.txt"

	perl -pe 's/{(.*?),\.(.*?)}/"<EM ID=\"'$arqSemExt'-".($numId++)(int(rand(1000)))."\" CATEG=\"$2\">$1<\/EM>"/eg' $arqSemExt"_anotado1.txt" > $arqSemExt"_anotado.txt"

	mv $arqSemExt"_anotado.txt" ../Entrada/$arqSemExt".txt" 

done

cd ..
	
