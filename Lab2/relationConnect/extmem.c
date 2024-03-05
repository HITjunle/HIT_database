/*
 * extmem.c
 * Zhaonian Zou
 * Harbin Institute of Technology
 * Jun 22, 2011
 */

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include "extmem.h"

Buffer *initBuffer(size_t bufSize, size_t blkSize, Buffer *buf)
{
    int i;

    buf->numIO = 0;
    buf->bufSize = bufSize;
    buf->blkSize = blkSize;
    buf->numAllBlk = bufSize / (blkSize + 1);
    buf->numFreeBlk = buf->numAllBlk;
    buf->data = (unsigned char*)malloc(bufSize * sizeof(unsigned char));

    if (!buf->data)
    {
        perror("Buffer Initialization Failed!\n");
        return NULL;
    }

    memset(buf->data, 0, bufSize * sizeof(unsigned char));
    return buf;
}

void freeBuffer(Buffer *buf)
{
    free(buf->data);
}

unsigned char *getNewBlockInBuffer(Buffer *buf)
{
    unsigned char *blkPtr;

    if (buf->numFreeBlk == 0)
    {
        perror("Buffer is full!\n");
        return NULL;
    }

    blkPtr = buf->data;

    while (blkPtr < buf->data + (buf->blkSize + 1) * buf->numAllBlk)
    {
        if (*blkPtr == BLOCK_AVAILABLE)
            break;
        else
            blkPtr += buf->blkSize + 1;
    }

    *blkPtr = BLOCK_UNAVAILABLE;
    buf->numFreeBlk--;
    return blkPtr + 1;
}

void freeBlockInBuffer(unsigned char *blk, Buffer *buf)
{
    *(blk - 1) = BLOCK_AVAILABLE;
    buf->numFreeBlk++;
}

int dropBlockOnDisk(unsigned int addr)
{
    char filename[40];

    sprintf(filename, "blks/%d.blk", addr);

    if (remove(filename) == -1)
    {
        perror("Dropping Block Fails!\n");
        return -1;
    }

    return 0;
}

//*将原始的循环条件 bytePtr < blkPtr + buf->blkSize 改为了 bytesRead < buf->blkSize
//*确保读取完整块：原始条件 bytePtr < blkPtr + buf->blkSize 是基于指针位置来确保不超出块的内存范围。而新条件 bytesRead < buf->blkSize 是直接基于读取的字节数。
unsigned char *readBlockFromDisk(unsigned int addr, Buffer *buf) {
    char filename[40];
    unsigned char *blkPtr, *bytePtr;
    int ch;

    if (buf->numFreeBlk == 0) {
        perror("Buffer Overflows!\n");
        return NULL;
    }

    blkPtr = buf->data;

    while (blkPtr < buf->data + (buf->blkSize + 1) * buf->numAllBlk) {
        if (*blkPtr == BLOCK_AVAILABLE)
            break;
        else
            blkPtr += buf->blkSize + 1;
    }

    sprintf(filename, "blks/%d.blk", addr);
    FILE *fp = fopen(filename, "rb");

    if (!fp) {
        perror("Reading Block Failed!\n");
        return NULL;
    }

    *blkPtr = BLOCK_UNAVAILABLE;
    blkPtr++;
    bytePtr = blkPtr;

    size_t bytesRead = 0;
    while ((ch = fgetc(fp)) != EOF && bytesRead < buf->blkSize) {
        *bytePtr++ = (unsigned char)ch;
        bytesRead++;
    }

    if (bytesRead < buf->blkSize) {
        perror("Not enough bytes read from the block.\n");
        fclose(fp);
        return NULL;
    }

    fclose(fp);
    buf->numFreeBlk--;
    buf->numIO++;

    return blkPtr;
}


int writeBlockToDisk(unsigned char *blkPtr, unsigned int addr, Buffer *buf)
{
    char filename[40];
    unsigned char *bytePtr;

    sprintf(filename, "blks/%d.blk", addr);
    FILE *fp = fopen(filename, "wb");

    if (!fp)
    {
        perror("Writing Block Failed!\n");
        return -1;
    }

    for (bytePtr = blkPtr; bytePtr < blkPtr + buf->blkSize; bytePtr++)
        fputc((int)(*bytePtr), fp);

    fclose(fp);
    *(blkPtr - 1) = BLOCK_AVAILABLE;
    buf->numFreeBlk++;
    buf->numIO++;
    return 0;
}
