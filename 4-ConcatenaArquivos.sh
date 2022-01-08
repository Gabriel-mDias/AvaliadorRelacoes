#!/bin/bash

cat cabecalho >> Col_anotado.xml

while read arq
do
	echo "<DOC DOCID=\"$arq\">" >> Col_anotado.xml
	cat Entrada/$arq.txt >> Col_anotado.xml
	echo "</DOC>" >> Col_anotado.xml
done < listaArquivos.txt

cat rodape >> Col_anotado.xml

sed -i "s;“;\";g; s;”;\";g; s;‘;';g; s;’;';g; s;…;...;g; s;–;-;g; s;—;-;g" Col_anotado.xml

iconv -f utf-8 -t iso-8859-1//TRANSLIT Col_anotado.xml > "CD$1_anotado.xml"

rm Col_anotado.xml

sed -i 's/&/&amp;/g' "CD$1_anotado.xml"

rm -r Entrada1

#firefox CD$1_anotado.xml
