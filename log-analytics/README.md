Run InsertScript with these inputs. (Inputs already embedded in the program). 

server=termspmr01.mayo.edu:9082,node=WebServiceREF1_pmr01,lastLogTime=[7/30/15 10:21:28:544 CDT]
server=termspmr04.mayo.edu:9083,node=WebServiceREF1_pmr04,lastLogTime=[7/17/15 8:48:12:510 CDT]
server=termspmr01.mayo.edu:9082,node=WebServiceREF2_pmr01,lastLogTime=[7/31/15 8:48:48:511 CDT]
server=termspmr04.mayo.edu:9083,node=WebServiceREF2_pmr04,lastLogTime=[7/31/15 8:48:53:548 CDT]

This shoulg result in the following records in the table.

termspmr01.mayo.edu:9082 WebServiceREF1_pmr01 1438269688544 
termspmr01.mayo.edu:9082 WebServiceREF2_pmr01 1438269688544 
termspmr04.mayo.edu:9083 WebServiceREF1_pmr04 1438269688544 
termspmr04.mayo.edu:9083 WebServiceREF2_pmr04 1438269688544 
				
Command line arguments while running Topology:
servers=termspmr04.mayo.edu:9083-WebServiceREF1_pmr04,termspmr01.mayo.edu:9082-WebServiceREF1_pmr01,termspmr04.mayo.edu:9083-WebServiceREF2_pmr04,termspmr01.mayo.edu:9082-WebServiceREF2_pmr01