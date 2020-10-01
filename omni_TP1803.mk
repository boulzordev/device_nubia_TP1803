# Inherit from those products. Most specific first.
$(call inherit-product, $(SRC_TARGET_DIR)/product/core_64_bit.mk)

# Get the prebuilt list of APNs
$(call inherit-product, vendor/omni/config/gsm.mk)

$(call inherit-product, $(SRC_TARGET_DIR)/product/aosp_base_telephony.mk)
$(call inherit-product, $(SRC_TARGET_DIR)/product/product_launched_with_p.mk)

TARGET_BOOTANIMATION_SIZE := 1080p

# Inherit from our custom product configuration
$(call inherit-product, vendor/omni/config/common.mk)

# Inherit from TP1803 device
$(call inherit-product, $(LOCAL_PATH)/device.mk)

DEVICE_PACKAGE_OVERLAYS += vendor/omni/overlay/CarrierConfig

PRODUCT_BRAND := nubia
PRODUCT_DEVICE := TP1803
PRODUCT_MANUFACTURER := nubia
PRODUCT_NAME := omni_TP1803
PRODUCT_MODEL := TP1803

PRODUCT_GMS_CLIENTID_BASE := android-nubia
TARGET_VENDOR := nubia
TARGET_VENDOR_PRODUCT_NAME := TP1803
PRODUCT_BUILD_PROP_OVERRIDES += PRIVATE_BUILD_DESC="TP1803-user 9 PKQ1.190504.001 eng.nubia.20190618.151633 release-keys"

VENDOR_RELEASE := 10/QQ3A.200805.001/6578210:user/release-keys
BUILD_FINGERPRINT := google/coral/coral:$(VENDOR_RELEASE)
OMNI_BUILD_FINGERPRINT := google/coral/coral:$(VENDOR_RELEASE)
OMNI_PRIVATE_BUILD_DESC := "'coral-user 10 QQ3A.200805.001 6578210 release-keys'"

PLATFORM_SECURITY_PATCH_OVERRIDE := 2020-08-05
