
 

 

 
segment .data

m_invite:	db "Entrez une chaine            : "
l_invite:	equ $ - m_invite
m_palind:	db "C'est un palindrome", 10
l_palind:	equ $ - m_palind
m_npalind:	db "Ce n'est pas un palindrome", 10
l_npalind:	equ $ - m_npalind
	
palindrome	dd 1    	
	
segment .bss

chaine:		resb 17	
longueur	resd 1	


segment .text
	global _start
	
_start:	
b_princ:
	
	mov eax, 4
	mov ebx, 1
	mov ecx, m_invite
	mov edx, l_invite
	int 0x80

	
	mov eax, 3
	mov ebx, 0
	mov ecx, chaine
	mov edx, 17
	int 0x80

	dec eax			  

	mov dword [longueur], eax 
	mov ecx, eax		  
	cmp ecx, 0
	je  aff_res		  


	mov ebx, chaine
	xor edx, edx

b_empile:
	mov dl, [ebx]
	push edx
	inc ebx
	loop b_empile



	mov ebx, chaine
	mov ecx, [longueur]
b_depile:
	pop edx
	cmp [ebx], dl 		
	je  suite
	mov dword [palindrome], 0 
suite:	inc ebx			
	loop b_depile

aff_res:
	
	cmp dword [palindrome], 1
	jne non_palind
	mov ecx, m_palind
	mov edx, l_palind
	jmp affiche

non_palind:
	mov ecx, m_npalind
	mov edx, l_npalind

affiche
	mov eax, 4
	mov ebx, 1
	int 0x80
	
quitter:
	mov eax, 1
	mov ebx, 0
	int 0x80
