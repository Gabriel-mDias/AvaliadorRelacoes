#!/bin/bash

cd Entrada_Lener

for i in * 
do
	#sed "s/<[^>]*>//g;" $i > $i.txt
	iconv -f iso-8859-1 -t utf-8 $i | sed '/^$/d' | sed  's;Art .;Art.;g; s;ART .;ART.;g; s;fl .;fl.;g; s;fls .;fls.;g; s;N .;N.;g; s;n .;n.;g;' > $i-utf
done

