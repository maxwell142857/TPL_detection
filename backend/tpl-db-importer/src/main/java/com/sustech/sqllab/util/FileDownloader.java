package com.sustech.sqllab.util;

import lombok.Cleanup;
import lombok.SneakyThrows;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import static java.util.concurrent.TimeUnit.SECONDS;

public class FileDownloader {

	private static final int TIME_OUT=30; //second
	private static final OkHttpClient CLIENT=new OkHttpClient.Builder()
												.connectTimeout(TIME_OUT, SECONDS)
												.readTimeout(TIME_OUT, SECONDS)
												.build();

	/**
	 * @param url 文件在网络上的URL
	 * @param path 下载到的本地目标路径
	 */
	@SuppressWarnings("ConstantConditions")
	@SneakyThrows(IOException.class)
	public static void downloadFile(String url, String path){
		Request request =new Request.Builder()
									.url(url)
									.get()
									.build();
		@Cleanup Response response = CLIENT.newCall(request).execute();
		byte[] bytes = response.body().bytes();
		Files.write(Path.of(path),bytes,StandardOpenOption.CREATE);
	}
}
