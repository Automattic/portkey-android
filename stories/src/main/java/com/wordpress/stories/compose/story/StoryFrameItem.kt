package com.wordpress.stories.compose.story

import android.net.Uri
import com.automattic.photoeditor.views.added.AddedView
import com.automattic.photoeditor.views.added.AddedViewList
import com.wordpress.stories.compose.frame.StorySaveEvents.SaveResultReason
import com.wordpress.stories.compose.frame.StorySaveEvents.SaveResultReason.SaveSuccess
import com.wordpress.stories.compose.story.StoryFrameItem.BackgroundSource.UriBackgroundSource
import com.wordpress.stories.compose.story.StoryFrameItemType.IMAGE
import com.wordpress.stories.compose.story.StoryFrameItemType.VIDEO
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ArrayListSerializer
import java.io.File

@Serializable
data class StoryFrameItem(
    val source: BackgroundSource,
    val frameItemType: StoryFrameItemType = IMAGE,
    @Serializable(with = AddedViewListSerializer::class)
    var addedViews: AddedViewList = AddedViewList(),
    var saveResultReason: SaveResultReason = SaveSuccess,
    @Serializable(with = FileSerializer::class)
    var composedFrameFile: File? = null,
    var id: String? = null
) {
    @Serializable
    sealed class BackgroundSource {
        @Serializable
        data class UriBackgroundSource(
            @Serializable(with = UriSerializer::class)
            var contentUri: Uri? = null
        ) : BackgroundSource()

        @Serializable
        data class FileBackgroundSource(
            @Serializable(with = FileSerializer::class)
            var file: File? = null
        ) : BackgroundSource()
    }

    @Serializer(forClass = Uri::class)
    object UriSerializer : KSerializer<Uri> {
        override fun deserialize(input: Decoder): Uri {
            return Uri.parse(input.decodeString())
        }

        override fun serialize(output: Encoder, obj: Uri) {
            output.encodeString(obj.toString())
        }
    }

    @Serializer(forClass = File::class)
    object FileSerializer : KSerializer<File> {
        override fun deserialize(input: Decoder): File {
            return File(input.decodeString())
        }

        override fun serialize(output: Encoder, obj: File) {
            output.encodeString(obj.toString())
        }
    }

    @Serializer(forClass = AddedViewList::class)
    object AddedViewListSerializer : KSerializer<AddedViewList> {
        override fun deserialize(input: Decoder): AddedViewList {
            val newList = AddedViewList()
            newList.addAll(input.decodeSerializableValue(ArrayListSerializer(AddedView.serializer())))
            return newList
        }

        override fun serialize(output: Encoder, addedViews: AddedViewList) {
            output.encodeSerializableValue(ArrayListSerializer(AddedView.serializer()), addedViews)
        }
    }

    companion object {
        @JvmStatic
        fun getNewStoryFrameItemFromUri(uri: Uri, isVideo: Boolean): StoryFrameItem {
            return StoryFrameItem(
                    UriBackgroundSource(uri),
                    if (isVideo) {
                        VIDEO(false)
                    } else {
                        IMAGE
                    }
            )
        }
    }
}
