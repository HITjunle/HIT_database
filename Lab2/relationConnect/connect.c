#include <stdlib.h>
#include <stdio.h>
#include "extmem.h"
#include <time.h>
#include <string.h>
#include <dirent.h>
#include <windows.h>
#define ADDRR 0
#define ADDRS 16
#define offset 56
#define HASH_SIZE 41 // 定义哈希桶的大小
#define TUPLES_PER_BLOCK 7 // 每个块中的元组数量/每个hash桶里面的最多元组数量，这边为了方便就定义成这个一样
#define capicity 1   //归并段数量 1 2 4 8
typedef struct {
	int first;
	int second;
}Tuple;

//! attention 在windows上需要注意的问题:
//! 貌似发的extmem是在macos下运行的，在linux和macos下读取文件的时候没有问题
//! 但是在windows下读写文件的时候，fopen要加上b，按照二进制文件打开，如果不加，windows文件系统会把某些特殊字符转换
//! 我观察blk文件发现某些块大小是66，某些是64.
//* update1 将磁盘结果写入blks文件夹
//* update2 readBlockFromDisk 
//* 添加了 bytesRead 变量：这个变量用于跟踪从文件中实际读取的字节数。
//* 修改了读取循环的条件：在原先的代码中，循环直到达到文件末尾（EOF）。在修改后的代码中，循环直到读取的字节数达到块的大小（buf->blkSize）或者文件结束（EOF）。这确保了即使在文件末尾之前也只读取了完整的块大小。
//* 增加了对读取不完整块的检查：如果读取的字节少于块的大小，函数会打印错误消息并返回 NULL。这是通过检查 bytesRead 是否小于 buf->blkSize 来实现的

//删除blks文件夹的blk，初始化
int DeleteFilesInFolder(const char* folderPath) {
    WIN32_FIND_DATA findFileData;
    char searchPath[1024];
    sprintf(searchPath, "%s\\*.*", folderPath);
    HANDLE hFind = FindFirstFile(searchPath, &findFileData);

    if (hFind == INVALID_HANDLE_VALUE) {
        return 1; // 返回错误代码
    } 

    do {
        if (!(findFileData.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY)) {
            char filePath[1024];
            sprintf(filePath, "%s\\%s", folderPath, findFileData.cFileName);
            DeleteFile(filePath); // 执行删除操作
        }
    } while (FindNextFile(hFind, &findFileData) != 0);

    FindClose(hFind);
    return 0; // 成功完成操作
}

// 生成关系R和S的数据
void generateData(Buffer buf) {
	unsigned char *blk;
	srand(1);
	// 生成关系R的数据,包含16 * 7 = 112个元组
	// R具有两个属性A和B，其中A和B的属性值均为int型（4个字节）。A的值域为[1, 40]，B的值域为[1, 1000]。
	for (int i = 0; i < 16; i++)
	{
		blk = getNewBlockInBuffer(&buf);
		for (int j = 0; j < 7; j++)
		{
			int a = rand() % 40 + 1;
			int b = rand() % 1000 + 1;
			memcpy(blk + j * 8, &a, 4);
			memcpy(blk + j * 8 + 4, &b, 4);
		}
		
		int nextBlkAddr_R = (i == 15) ? -1 : i+1;
		
		memcpy(blk + 56,&nextBlkAddr_R,sizeof(int)); //写入下一块的地址
		writeBlockToDisk(blk,i,&buf); //R块地址从0开始

		
	}

	//生成关系S的数据,包含32 * 7 = 224个元组
	//关系S具有两个属性C和D，其中C和D的属性值均为int型（4个字节）。C的值域为[20, 60]，D的值域为[1, 1000]。
	for (int i = 0; i < 32; i++)
	{
		blk = getNewBlockInBuffer(&buf);
		for (int j = 0; j < 7; j++)
		{
			int c = rand() % 41 + 20;
			int d = rand() % 1000 + 1;
			memcpy(blk + j * 8, &c, 4);
			memcpy(blk + j * 8 + 4, &d, 4);
		}
		int nextBlkAddr_S = (i == 31) ? -1 : i+17;
		memcpy(blk+56,&nextBlkAddr_S,sizeof(int)); //写入下一块的地址
		writeBlockToDisk(blk,i+16,&buf); //S块地址从16开始
		
	}

}

// 实现选择算法
//*index = 0 addr = ADDRR: select R.A
//*index = 1 addr = ADDRR: select R.B
//*index = 0 addr = ADDRS: select S.C
//*index = 1 addr = ADDRS: select S.D
//*select 选择的数字
void selectOperation(Buffer buf, int addr,int select,int index) {
    unsigned char *blk, *outBlk;
	int outAddr = 500; //选择结果存放的地址
	if (addr == ADDRR)
	{
		if (index == 0)
		{
			printf("R.A = %d: ",select);
		}
		else
		{
			printf("R.B = %d: ",select);
		}
		
	}
	else if (addr == ADDRS)
	{
		if (index == 0)
		{
			printf("S.C = %d: ",select);
		}
		else
		{
			printf("S.D = %d: ",select);
		}
	}
	
	
	do
	{
		blk = readBlockFromDisk(addr,&buf);
		outBlk = getNewBlockInBuffer(&buf);//申请一个新的块存投影结果
		int a;
		for (int j = 0; j < 7; j++)
		{	
			memcpy(&a,blk + j * 8 + 4 * index, 4);
			if (a == select)
			{
				printf("(%-2d,%-2d) ",*(int *)(blk + j*8 ),*(int *)(blk + j*8 + 4));
			}
		}
		memcpy(&addr, blk + 56, 4);
		writeBlockToDisk(outBlk,outAddr++,&buf);
		freeBlockInBuffer(blk,&buf);//释放块
		
	}while(addr != -1);
	
	
}





// 实现投影算法
void projectOperation(Buffer buf, int addr) {
    unsigned char *blk,*outBlk;
	int projected[112] = {0};
	int k = 0;
	int outAddr = 1000; //投影结果存放的地址
	
	do
	{
		blk = readBlockFromDisk(addr,&buf);
		addr = *(int *)(blk + 56); //读取下一块的地址
		outBlk = getNewBlockInBuffer(&buf);//申请一个新的块存投影结果
		for (int j = 0; j < 7; j++) {
			// 检查当前值是否已存在于projected数组中
            int duplicate = 0;
			for (int i = 0; i < k; i++)
			{
				int cur = *(int *)(blk + j * 8);
				if (cur == projected[i])
				{
					duplicate = 1;
					break;
				}
			}
			if (!duplicate)
			{
				memcpy(&projected[k++],blk + j * 8, 4);
			}
			
		}
		//写入到磁盘中
		writeBlockToDisk(outBlk,outAddr++,&buf);
		freeBlockInBuffer(blk,&buf);//释放当前块

	}while (addr != -1);
	printf("\nRelation R's A attribute is:\n");
	printf("----------------------------\n");
	for (int i = 0; i < 112; i++)
	{
		if (projected[i] == 0)
		{
			break;
		}
		
		printf("|%2d",projected[i]);
	}
	printf("|\n");
	printf("----------------------------\n");
	
	
}

//nested-loop join算法
void nestedLoopJoin(Buffer * buf, int addrR, int addrS) {
    unsigned char *blkR, *blkS, *outBlk;
	int outAddr = 2000;
	outBlk = getNewBlockInBuffer(buf);//申请一个新的块存join结果
	int numTuple = 0; // 记录输出块中的元组数量
	printf("|R.A |R.B |S.C |S.D | \n");
	printf("----------------------------\n");
	int temp_addrS = addrS;//保存addrS的初始值，用于每次开始遍历
	do
	{
		blkR = readBlockFromDisk(addrR,buf);
		addrR = *(int *)(blkR + 56); //读取下一块的地址
		addrS = temp_addrS;
		do {
			blkS = readBlockFromDisk(addrS,buf);
			addrS = *(int *)(blkS + 56); //读取下一块的地址
			for (int m = 0; m < 7; m++)
			{
				int a = *(int *)(blkR + 8*m);
				for (int n = 0; n < 7; n++)
				{
					int c = *(int *)(blkS + 8*n);

					if (a == c)
					{
						int b = *(int *)(blkR + 8*m + 4);
						int d = *(int *)(blkS + 8*n + 4);
						
						printf("|%4d|%4d|%4d|%4d|\n",a,b,c,d);
						memcpy(outBlk+numTuple*16,&a,4); //写入R.A
						memcpy(outBlk+numTuple*16+4,&b,4); //写入R.B
						memcpy(outBlk+numTuple*16+8,&c,4); //写入S.C
						memcpy(outBlk+numTuple*16+12,&d,4); //写入S.D
						numTuple++;
						if (numTuple == 4)
						{
							writeBlockToDisk(outBlk,outAddr++,buf);//写入磁盘
							numTuple = 0;
							outBlk = getNewBlockInBuffer(buf);//申请一个新的块存join结果
						}
						
					}
					
				}

			}
			freeBlockInBuffer(blkS,buf);//释放当前块
		}while (addrS != -1);
		freeBlockInBuffer(blkR,buf);//释放当前块
	}while (addrR != -1);
	

}

//hash join算法


// 哈希函数
int hashFunction(int value) {
    return value % HASH_SIZE;
}

void hashJoin(Buffer *buf, int addrR, int addrS) {
    unsigned char *blkR, *blkS, *outBlk;
    int outAddr = 3000;
    int numTuple = 0;
	outBlk = getNewBlockInBuffer(buf);//申请一个新的块存join结果
    Tuple bucketR[HASH_SIZE][TUPLES_PER_BLOCK] = {0}; // 哈希桶
    int bucketCountR[HASH_SIZE] = {0};

    printf("|R.A |R.B |S.C |S.D | \n");
    printf("----------------------------\n");

    // 填充R关系的哈希桶
    do {
        blkR = readBlockFromDisk(addrR, buf);
        for (int i = 0; i < 7; i++) {
            int a = *(int *)(blkR + 8 * i);
            int b = *(int *)(blkR + 8 * i + 4);
            int hashValue = hashFunction(a);
			//当桶装得下元组的时候，就把元组放进去
            if (bucketCountR[hashValue] < TUPLES_PER_BLOCK) {
                bucketR[hashValue][bucketCountR[hashValue]].first = a;
                bucketR[hashValue][bucketCountR[hashValue]].second = b;
                bucketCountR[hashValue]++;
            }
        }
        addrR = *(int *)(blkR + 56);
        freeBlockInBuffer(blkR, buf);
    } while (addrR != -1);

    // hash join
    do {
        blkS = readBlockFromDisk(addrS, buf);
        for (int i = 0; i < 7; i++) {
            int c = *(int *)(blkS + 8 * i);
            int d = *(int *)(blkS + 8 * i + 4);
            int hashValue = hashFunction(c);
			for (int  k = 0; k < bucketCountR[hashValue]; k++) {
				int a = bucketR[hashValue][k].first;
				//满足R.A = S.C
				if (a == c)
				{
					int b = bucketR[hashValue][k].second;
					printf("|%4d|%4d|%4d|%4d|\n", a, b, c, d);
	                memcpy(outBlk + numTuple * 16, &a, 4); // 写入R.A
	                memcpy(outBlk + numTuple * 16 + 4, &b, 4); // 写入R.B
	                memcpy(outBlk + numTuple * 16 + 8, &c, 4); // 写入S.C
	                memcpy(outBlk + numTuple * 16 + 12, &d, 4); // 写入S.D
	                numTuple++;
	                if (numTuple == 4) {
	                    writeBlockToDisk(outBlk, outAddr++, buf);
	                    numTuple = 0;
	                    outBlk = getNewBlockInBuffer(buf);
	                }
				}
				
			}	
			}

        addrS = *(int *)(blkS + 56);
        freeBlockInBuffer(blkS, buf);
    } while (addrS != -1);
}

//sortMergeJoin

int compareTuples(const void *a, const void *b) {
    Tuple *tupleA = (Tuple *)a;
    Tuple *tupleB = (Tuple *)b;
    if (tupleA->first < tupleB->first) return -1;
    if (tupleA->first > tupleB->first) return 1;
    return 0;
}
//*args: buf,addr,arr,len
//*addr:关系的起始地址
//*arr:存放关系的数组
//*len:关系的个数
//*排序从addr开始的关系，关系的个数为len。排序结果存在arr数组中
void sortRelation(Buffer *buf, int addr, Tuple * arr,int len) {
	unsigned char *blk;
	int i = 0;
	do {
		blk = readBlockFromDisk(addr, buf);
		for (int j = 0; j < 7; j++)
		{
			arr[i].first = *(int *)(blk + j * 8);
			arr[i].second = *(int *)(blk + j * 8 + 4);
			i++;
		}
		addr = *(int *)(blk + 56);
		freeBlockInBuffer(blk, buf);
	} while (addr != -1 && i < len);
	qsort(arr, len, sizeof(Tuple), compareTuples);

	
}



void sortMergeJoin(Buffer *buf, int addrR, int addrS) {
	unsigned char *blkR, *blkS, *outBlk;
	int outAddr = 4000;
	int numTuple = 0;
	outBlk = getNewBlockInBuffer(buf);//申请一个新的块存join结果
	printf("|R.A |R.B |S.C |S.D | \n");
    printf("----------------------------\n");

    // 对R关系进行排序
	Tuple * arrR = malloc(sizeof(Tuple) * 112);
	sortRelation(buf, addrR, arrR,112);
	// 对S关系进行排序
	Tuple * arrS = malloc(sizeof(Tuple) * 224);
	sortRelation(buf, addrS, arrS,224);
	int i = 0, j = 0;
	while (i < 112 && j < 224)
	{
		if (arrR[i].first < arrS[j].first)
		{
			i++;
		}
		else if (arrR[i].first == arrS[j].first)
		{
			int temp = arrR[i].first;
			int temp_j = j;
			while (arrR[i].first == temp)
			{
				j = temp_j;
				while (arrS[j].first == temp)
				{
					int a = arrR[i].first;
	                int b = arrR[i].second;
	                int c = arrS[j].first;
	                int d = arrS[j].second;
					printf("|%4d|%4d|%4d|%4d|\n", a, b, c, d);
		            memcpy(outBlk + numTuple * 16, &a, 4); // 写入R.A
		            memcpy(outBlk + numTuple * 16 + 4, &b, 4); // 写入R.B
		            memcpy(outBlk + numTuple * 16 + 8, &c, 4); // 写入S.C
		            memcpy(outBlk + numTuple * 16 + 12, &d, 4); // 写入S.D
		            numTuple++;
		            if (numTuple == 4) {
		                writeBlockToDisk(outBlk, outAddr++, buf);
		                numTuple = 0;
		                outBlk = getNewBlockInBuffer(buf);
		            }
					j++;
				}
				i++;
				
			}
		}
		else
		{
			j++;
		}
	}
}

//*支持多个归并段
void mergeJoin(Buffer *buf, int addrR, int addrS) {
	unsigned char *blkR, *blkS, *outBlk;
	int outAddr = 5000;
	int numTuple = 0;
	outBlk = getNewBlockInBuffer(buf);//申请一个新的块存join结果
	printf("|R.A |R.B |S.C |S.D | \n");
    printf("----------------------------\n");
	int lenR = 112 / capicity;
	int lenS = 224 / capicity;
	
	Tuple * arrS = malloc(sizeof(Tuple) * lenS);
	
	for (int k = 0; k < capicity; k++)
	{
		Tuple * arrR = malloc(sizeof(Tuple) * lenR);
		int new_addR = addrR + 16/capicity * k;
		sortRelation(buf, new_addR, arrR,lenR);
		for (int l = 0; l < capicity; l++)
		{
			Tuple * arrS = malloc(sizeof(Tuple) * lenS);
			int new_addS = addrS + 32/capicity * l;
			sortRelation(buf, new_addS, arrS,lenS);
			int i = 0, j = 0;
			while (i < lenR && j < lenS)
			{
				if (arrR[i].first < arrS[j].first)
				{
					i++;
				}
				else if (arrR[i].first == arrS[j].first)
				{
					int temp = arrR[i].first;
					int temp_j = j;
					while (arrR[i].first == temp)
					{
						j = temp_j;
						while (arrS[j].first == temp)
						{
							int a = arrR[i].first;
			                int b = arrR[i].second;
			                int c = arrS[j].first;
			                int d = arrS[j].second;
							printf("|%4d|%4d|%4d|%4d|\n", a, b, c, d);
				            memcpy(outBlk + numTuple * 16, &a, 4); // 写入R.A
				            memcpy(outBlk + numTuple * 16 + 4, &b, 4); // 写入R.B
				            memcpy(outBlk + numTuple * 16 + 8, &c, 4); // 写入S.C
				            memcpy(outBlk + numTuple * 16 + 12, &d, 4); // 写入S.D
				            numTuple++;
				            if (numTuple == 4) {
				                writeBlockToDisk(outBlk, outAddr++, buf);
				                numTuple = 0;
				                outBlk = getNewBlockInBuffer(buf);
				            }
							j++;
						}
						i++;
						
					}
				}
				else
				{
					j++;
				}
			}
			free(arrS);
		}
		free(arrR);
	}

}




int main(int argc, char **argv) {
	Buffer buf; /* A buffer */
    unsigned char *blk; /* A pointer to a block */
	DeleteFilesInFolder("blks");
	if (!initBuffer(520, 64, &buf)) {
        perror("Buffer Initialization Failed.\n");
        return -1;
    }
	generateData(buf);
	printf("\n read: ");
	for (int i = 0; i < 16; i++) {
	    unsigned char *blk = readBlockFromDisk(i, &buf);
	    int read;
	    memcpy(&read, blk + 56, 4); // 使用memcpy来读取下一块的地址
	    printf("%d ", read);
	    freeBlockInBuffer(blk, &buf);
	}
	printf("\n");
	//*selectOperation(buf,addr,selectNum,index)
	//*eg: SELECT * from ... WHERE R.A=40 
	selectOperation(buf,ADDRR,40,0);
	printf("\n");
	selectOperation(buf,ADDRS,60,0);
	
	projectOperation(buf,ADDRR);
	int n ;
	printf("请输入连接方式(嵌套连接0 哈希连接1 归并排序连接2)\n");
	scanf("%d",&n);
	if (n == 0)
	{
		printf("nested loop join\n");
		nestedLoopJoin(&buf,ADDRR,ADDRS);
		printf("\n----------------------------\n");
	}
	
	else if (n == 1)
	{
		printf("hash join\n");
		hashJoin(&buf,ADDRR,ADDRS);
		printf("\n----------------------------\n");
	}
	else if (n == 2)
	{
		printf("sortMerge join\n");
		mergeJoin(&buf,ADDRR,ADDRS);
	}
	


	// printf("%ld\n",buf.numIO); //输出IO次数	
	// return 0;

}


