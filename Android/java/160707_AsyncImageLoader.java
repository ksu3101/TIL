/**
 * @author KangSung-Woo
 * @since 2016/06/16
 */
public class AsyncImageLoader {
  public static final  int    DEFAULT_FADE_DURATION = 250;
  private static final String TAG                   = AsyncImageLoader.class.getSimpleName();
  private static final int    DEFAULT_COLOR         = Color.WHITE;
  private AsyncTask<Void, Void, Bitmap>       imgLoader;
  private Callback                            callback;
  private boolean                             isFitImageView;
  private boolean                             isEnableResize;
  private int                                 width;
  private int                                 height;
  private int                                 fadeAnimDuration;
  private ImageView.ScaleType                 scaleType;
  private boolean                             isEnableFade;
  private boolean                             isEnableRoundedRect;
  private Drawable                            placeHolderDrawable;
  private Drawable                            errorDrawable;
  private com.squareup.picasso.Transformation transformation;
  private ColorDrawable                       beforeTransitionDrawable;
  private boolean                             isPrintLog;

  private AsyncImageLoader(Builder builder) {
    this.callback = builder.callback;
    this.width = builder.width;
    this.height = builder.height;
    this.fadeAnimDuration = builder.fadeAnimDuration;
    this.isEnableResize = builder.isResizeEnable;
    this.scaleType = builder.scaleType;
    this.isEnableFade = builder.isEnableFade;
    this.placeHolderDrawable = builder.placeHolderDrawable;
    this.errorDrawable = builder.errorDrawable;
    this.transformation = builder.transformation;
    this.isFitImageView = builder.isFitImageView;
    this.isPrintLog = builder.isPrintLog;
    this.beforeTransitionDrawable = builder.beforeTransitionDrawable;

    getImageFromNetwork(builder.imgUrl, builder.imageView);
  }

  private void getImageFromNetwork(final String imageUrl, final ImageView imageView) {
    if (!TextUtils.isEmpty(imageUrl) && Utils.isImagePath(imageUrl)) {
      imgLoader = new AsyncTask<Void, Void, Bitmap>() {
        @Override
        protected void onPreExecute() {
          if (callback != null) {
            callback.onStarted();
          }
          if (imageView != null) {
            if (isFitImageView) {
              isEnableResize = true;
              width = imageView.getWidth();
              height = imageView.getHeight();
            }
            if (placeHolderDrawable != null) {
              imageView.setImageDrawable(placeHolderDrawable);
            }
            if (scaleType != null) {
              imageView.setScaleType(scaleType);
            }
          }
        }

        @Override
        protected Bitmap doInBackground(Void... params) {
          Bitmap result = null;
          BufferedInputStream bis = null;
          try {
            URL url = new URL(imageUrl);
            URLConnection con = url.openConnection();
            con.connect();

            // get file size
            final int lengthOfFile = con.getContentLength();

            bis = new BufferedInputStream(con.getInputStream());

            result = BitmapFactory.decodeStream(bis);
            bis.close();

            if (isPrintLog) {
              Log.d(TAG, "// LOG // lengthOfFile = " + lengthOfFile + ", width = " + width + ", height = " + height + ", loaded Bitmap image width = " +
                  (result != null ? result.getWidth() : "NaN") + ", image height = " + (result != null ? result.getHeight() : "Nan")
              );
            }

            if (result != null) {
              if (isEnableResize) {
                final int w = result.getWidth();
                final int h = result.getHeight();
                if (w < h) {
                  width = height;
                  height = -1;
                }

                if (width <= 0) {
                  if (isPrintLog) {
                    Log.w(TAG, "이미지 리사이징 될 너비는 최소한 0보다 커야 합니다.");
                  }
                  throw new IllegalArgumentException("이미지 리사이징 될 너비는 최소한 0보다 커야 합니다.");
                }
                else {
                  if (height <= 0) {
                    // 될 수 있으면 ratio에 맞춘 리사이징을 하도록 유도 한다.
                    // 유도 후 scaletype에 맞추어 이미지뷰에 세팅 한다.
                    result = Utils.getResizeImg(result, width);
                  }
                  else {
                    result = Bitmap.createScaledBitmap(result, width, height, true);
                  }
                }
              }

              if (transformation != null) {
                result = transformation.transform(result);
              }
            }
            else {
              onError(imageView, "Result bitmap image is Null..", null);
            }

          } catch (Exception e) {
            onError(imageView, e.getMessage(), e);
          }
          return result;
        }

        @Override
        protected void onPostExecute(Bitmap resultImg) {
          if (imageView != null && resultImg != null) {
            if (isEnableFade) {
              // start Fade animations
              imageView.clearAnimation();
              final Drawable beforeDrawable = imageView.getDrawable();
              final TransitionDrawable transitionDrawable =
                  Utils.createTransitionDrawable(
                      (beforeDrawable == null ? (beforeTransitionDrawable != null ? beforeTransitionDrawable : new ColorDrawable(DEFAULT_COLOR)) : beforeDrawable),
                      new BitmapDrawable(resultImg)
                  );
              if (transitionDrawable != null) {
                imageView.setImageDrawable(transitionDrawable);
                transitionDrawable.startTransition(fadeAnimDuration);
              }
              else {
                imageView.setImageBitmap(resultImg);
              }
            }
            else {
              imageView.setImageBitmap(resultImg);
            }
            if (imageView.getVisibility() != View.VISIBLE) {
              imageView.setVisibility(View.VISIBLE);
            }
            imageView.setImageBitmap(resultImg);
          }
          if (callback != null) {
            callback.onLoadCompleted(resultImg);
          }
        }
      };
      imgLoader.execute();
    }
    else {
      // 이미지 경로가 아님 -> 이미지뷰를 숨기고 오류 로그 출력
      onError(imageView, true, "\"" + imageUrl + "\" -> 이미지 파일 경로가 아닙니다.", null);
    }
  }

  private void onError(@NonNull ImageView iv, String errorMsg, Exception e) {
    onError(iv, false, errorMsg, e);
  }

  private void onError(@NonNull ImageView iv, boolean hideImageView, String errorMsg, Exception e) {
    if (hideImageView) {
      iv.setVisibility(View.GONE);
    }
    else {
      if (errorDrawable != null) {
        iv.setImageDrawable(errorDrawable);
      }
      if (iv.getVisibility() != View.VISIBLE) {
        iv.setVisibility(View.VISIBLE);
      }
    }

    if (callback != null) {
      callback.onError(errorMsg, e);
    }
  }

  /**
   * 생성된 AsyncTask가 동작중인지 여부를 얻는다.
   *
   * @return true일 경우 AsyncTask가 아직 동작 중.
   */
  public boolean isRunning() {
    if (imgLoader != null) {
      return (imgLoader.getStatus() == AsyncTask.Status.RUNNING);
    }
    return false;
  }

  /**
   * 생성된 AsyncTask를 중지 한다.
   */
  public void cancel() {
    if (imgLoader != null) {
      imgLoader.cancel(true);
    }
  }

  public interface Callback {
    /**
     * 이미지를 비동기 로드 하기 전에 불리는 메소드
     */
    void onStarted();

    /**
     * 이미지를 불러오는 중 오류 발생시 불리는 메소드
     *
     * @param errorMsg 오류 메시지
     * @param e        오류에 대한 예외 객체
     */
    void onError(String errorMsg, Exception e);

    /**
     * 비동기로 이미지를 불리고 난 뒤에 불리는 메소드
     *
     * @param bmp 불리어진 이미지의 비트맵 객체 혹은 null.
     */
    void onLoadCompleted(Bitmap bmp);
  }

  public static class Builder {
    // 필수
    private String    imgUrl;
    private ImageView imageView;

    // 옵션
    private boolean                             isFitImageView;
    private boolean                             isResizeEnable;
    private ImageView.ScaleType                 scaleType;
    private boolean                             isEnableFade;
    private int                                 width;
    private int                                 height;
    private int                                 fadeAnimDuration;
    private Callback                            callback;
    private Drawable                            placeHolderDrawable;
    private Drawable                            errorDrawable;
    private com.squareup.picasso.Transformation transformation;
    private boolean                             isPrintLog;
    private ColorDrawable                       beforeTransitionDrawable;

    public Builder() {
      this.imgUrl = "";
      this.isResizeEnable = false;
      this.scaleType = ImageView.ScaleType.FIT_START;
      this.isEnableFade = true;
      this.fadeAnimDuration = DEFAULT_FADE_DURATION;
      this.width = 0;
      this.height = 0;
      this.imageView = null;
      this.callback = null;
      this.placeHolderDrawable = new ColorDrawable(DEFAULT_COLOR);
      this.errorDrawable = null;
      this.isFitImageView = false;
      this.isPrintLog = false;
      this.beforeTransitionDrawable = new ColorDrawable(DEFAULT_COLOR);
    }

    /**
     * 불러올 이미지의 URL을 설정 한다.
     */
    public Builder targetUrl(@NonNull String url) {
      this.imgUrl = url;
      return this;
    }

    /**
     * 불러온 이미지를 리사이징 한다.
     */
    public Builder resize(@IntRange(from = 1) int width, @IntRange(from = 1) int height) {
      this.width = width;
      this.height = height;
      isResizeEnable = true;
      return this;
    }

    /**
     * 불러온 이미지의 큰 값(너비, 높이)에서 계산된 ratio비율에 맞추어 리사이징 한다.
     */
    public Builder resize(@IntRange(from = 1) int desireValue) {
      this.width = desireValue;
      this.height = -1;
      isResizeEnable = true;
      return this;
    }

    /**
     * 이미지를 불러 오는 중 오류 발생시의 이미지를 설정 한다.
     */
    public Builder error(@NonNull Drawable drawable) {
      this.errorDrawable = drawable;
      return this;
    }

    /**
     * 이미지를 불러 오는 중 오류 발생시의 이미지를 설정 한다.
     */
    public Builder error(@NonNull Context context, @DrawableRes int resId) {
      this.errorDrawable = ContextCompat.getDrawable(context, resId);
      return this;
    }

    /**
     * 이미지를 불러 오기 전 보여질 이미지를 설정 한다.
     */
    public Builder placeHolder(@NonNull Drawable drawable) {
      this.placeHolderDrawable = drawable;
      return this;
    }

    /**
     * 이미지를 불러 오기 전 보여질 이미지를 설정 한다.
     */
    public Builder placeHolder(@NonNull Context context, @DrawableRes int resId) {
      this.placeHolderDrawable = ContextCompat.getDrawable(context, resId);
      return this;
    }

    /**
     * ImageView의 크기에 맞추어 불러올 이미지를 리 사이징 한다.
     */
    public Builder fit() {
      this.isFitImageView = true;
      return this;
    }

    /**
     * 불러온 이미지를 이미지뷰에 세팅 할때 {@code CENTER_CROP}으로 설정 한다.
     *
     * @see android.widget.ImageView.ScaleType
     */
    public Builder centerCrop() {
      this.scaleType = ImageView.ScaleType.CENTER_CROP;
      return this;
    }

    /**
     * 불러온 이미지를 이미지뷰에 세팅 할때 {@code CENTER_INSIDE}으로 설정 한다.
     *
     * @see android.widget.ImageView.ScaleType
     */
    public Builder centerInside() {
      this.scaleType = ImageView.ScaleType.CENTER_INSIDE;
      return this;
    }

    /**
     * 불러온 이미지를 이미지뷰에 세팅 할때 {@code FIT_XY}으로 설정 한다.
     *
     * @see android.widget.ImageView.ScaleType
     */
    public Builder fitXY() {
      this.scaleType = ImageView.ScaleType.FIT_XY;
      return this;
    }

    /**
     * 불러온 이미지를 이미지뷰에 세팅 할때의 스케일 타입을 설정 한다.
     *
     * @see android.widget.ImageView.ScaleType
     */
    public Builder scaleType(@NonNull ImageView.ScaleType scaleType) {
      this.scaleType = scaleType;
      return this;
    }

    /**
     * 이미지를 불러오고 난 뒤에 보여질 페이드 애니메이션 효과를 사용하지 않는다.
     */
    public Builder disableFadeAnimation() {
      this.isEnableFade = false;
      return this;
    }

    /**
     * 이미지를 불러오고 난 뒤에 보여질 페이드 애니메이션 효과를 사용한다.
     */
    public Builder enableFadeAnimation() {
      this.isEnableFade = true;
      return this;
    }

    /**
     * 이미지를 불러오고 난 뒤에 보여질 페이드 애니메이션의 duration을 설정 한다.
     */
    public Builder fadeAnimationDuration(int duration) {
      this.fadeAnimDuration = duration;
      return this;
    }

    /**
     * 이미지를 불러오기전에 보여질 color를 설정 한다. {@code placeHolder()}메소드와 사용에 유의 할 것.
     */
    public Builder fadeAnimationBeforeColor(@ColorInt int color) {
      this.beforeTransitionDrawable = new ColorDrawable(color);
      return this;
    }

    /**
     * 이미지를 불러오기전에 보여질 color를 설정 한다. {@code placeHolder()}메소드와 사용에 유의 할 것.
     */
    public Builder fadeAnimationBeforeColor(@NonNull Context context, @ColorRes int resId) {
      this.beforeTransitionDrawable = new ColorDrawable(ContextCompat.getColor(context, resId));
      return this;
    }

    /**
     * Picasso에서 사용되던 이미지 후처리 Transformation의 구현체를 설정 한다.
     *
     * @see com.squareup.picasso.Transformation
     */
    public Builder transform(com.squareup.picasso.Transformation transform) {
      this.transformation = transform;
      return this;
    }

    /**
     * 로그를 출력하게 한다.
     */
    public Builder printLog() {
      this.isPrintLog = true;
      return this;
    }

    /**
     * ImageView에 이미지를 비동기로 불러 오게 한다. 시작과 끝의 과정들을 callback구현체를 통해서 결과들을 받는다.
     */
    public AsyncImageLoader into(@NonNull ImageView imageView, Callback callback) {
      this.callback = callback;
      this.imageView = imageView;
      return new AsyncImageLoader(this);
    }

    /**
     * ImageView에 이미지를 비동기로 불러 오게 한다.
     */
    public AsyncImageLoader into(@NonNull ImageView imageView) {
      this.imageView = imageView;
      return new AsyncImageLoader(this);
    }

  }

}

